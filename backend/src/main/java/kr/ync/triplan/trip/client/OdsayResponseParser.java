package kr.ync.triplan.trip.client;

import kr.ync.triplan.trip.dto.response.TransitRouteResponse.Route;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse.Step;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse.StepType;
import kr.ync.triplan.trip.exception.TransitApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

// 오디세이 응답 JSON을 우리 응답 형식으로 바꾼다
// - 성공: result.path[] (소요시간순이 아니라서 직접 정렬)
// - 실패: HTTP 200에 error가 담겨 온다. 검색 오류는 {"error":{"code","msg"}}, 인증 오류는 {"error":[{"code","message"}]}
@Component
@RequiredArgsConstructor
public class OdsayResponseParser {

	// 경로가 없는 경우로 보고 빈 목록을 돌려줄 오류 코드 (-98: 출발 · 도착지가 700m 이내, -99: 검색 결과 없음)
	private static final Set<String> NO_ROUTE_CODES = Set.of("-98", "-99");
	private static final int MAX_ROUTES = 3;

	// 오디세이 구간 종류 (trafficType)
	private static final int SUBWAY = 1;
	private static final int BUS = 2;

	private final ObjectMapper objectMapper;

	public List<Route> parse(String json) {
		JsonNode root;
		try {
			root = objectMapper.readTree(json == null ? "" : json);
		} catch (JacksonException e) {
			throw new TransitApiException();
		}

		JsonNode error = root.path("error");
		if (!error.isMissingNode()) {
			String code = error.isArray()
					? error.path(0).path("code").asString()
					: error.path("code").asString();
			if (NO_ROUTE_CODES.contains(code)) {
				return List.of();
			}
			throw new TransitApiException();
		}

		JsonNode paths = root.path("result").path("path");
		if (!paths.isArray()) {
			throw new TransitApiException();
		}

		List<Route> routes = new ArrayList<>();
		for (int i = 0; i < paths.size(); i++) {
			routes.add(toRoute(paths.get(i)));
		}
		return routes.stream()
				.sorted(Comparator.comparingInt(Route::totalTime))
				.limit(MAX_ROUTES)
				.toList();
	}

	private Route toRoute(JsonNode path) {
		JsonNode info = path.path("info");
		// busTransitCount · subwayTransitCount는 탄 횟수라서, 환승 횟수는 (탄 횟수 - 1)
		int rideCount = info.path("busTransitCount").asInt() + info.path("subwayTransitCount").asInt();

		List<Step> steps = new ArrayList<>();
		JsonNode subPaths = path.path("subPath");
		for (int i = 0; i < subPaths.size(); i++) {
			steps.add(toStep(subPaths.get(i)));
		}

		return new Route(
				info.path("totalTime").asInt(),
				info.path("payment").asInt(),
				Math.max(rideCount - 1, 0),
				info.path("totalWalk").asInt(),
				steps
		);
	}

	private Step toStep(JsonNode subPath) {
		int trafficType = subPath.path("trafficType").asInt();
		int time = subPath.path("sectionTime").asInt();
		if (trafficType == SUBWAY) {
			return new Step(StepType.SUBWAY, laneNames(subPath, "name"),
					subPath.path("startName").asString(), subPath.path("endName").asString(), time);
		}
		if (trafficType == BUS) {
			return new Step(StepType.BUS, laneNames(subPath, "busNo"),
					subPath.path("startName").asString(), subPath.path("endName").asString(), time);
		}
		return new Step(StepType.WALK, null, null, null, time);
	}

	// 한 구간에 탈 수 있는 노선이 여러 개면 쉼표로 이어 붙인다 (예: 버스 "402, 405")
	private String laneNames(JsonNode subPath, String field) {
		JsonNode lanes = subPath.path("lane");
		List<String> names = new ArrayList<>();
		for (int i = 0; i < lanes.size(); i++) {
			names.add(lanes.get(i).path(field).asString());
		}
		return String.join(", ", names);
	}
}

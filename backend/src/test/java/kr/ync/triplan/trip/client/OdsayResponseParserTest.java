package kr.ync.triplan.trip.client;

import kr.ync.triplan.trip.dto.response.TransitRouteResponse.Route;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse.Step;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse.StepType;
import kr.ync.triplan.trip.exception.TransitApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static kr.ync.triplan.support.OdsayFixture.AUTH_FAILED_JSON;
import static kr.ync.triplan.support.OdsayFixture.SUCCESS_JSON;
import static kr.ync.triplan.support.OdsayFixture.TOO_CLOSE_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

// 오디세이 응답 해석만 확인하는 단위 테스트 (스프링 · DB 없이 실행)
class OdsayResponseParserTest {

	private final OdsayResponseParser parser = new OdsayResponseParser(JsonMapper.builder().build());

	@Test
	@DisplayName("성공 응답 - 소요시간이 짧은 순으로 최대 3개")
	void parse_sortedAndLimited() {
		List<Route> routes = parser.parse(SUCCESS_JSON);

		assertThat(routes).extracting(Route::totalTime).containsExactly(33, 36, 39);
	}

	@Test
	@DisplayName("성공 응답 - 요금 · 도보 거리, 환승 횟수는 (탄 횟수 - 1)")
	void parse_routeInfo() {
		List<Route> routes = parser.parse(SUCCESS_JSON);

		assertThat(routes)
				.extracting(Route::payment, Route::walkDistance, Route::transferCount)
				.containsExactly(
						tuple(1650, 227, 1),  // 지하철 2번 → 환승 1번
						tuple(2500, 274, 0),  // 버스 1번 → 환승 없음
						tuple(1650, 680, 1)); // 버스 + 지하철 → 환승 1번
	}

	@Test
	@DisplayName("성공 응답 - 구간은 지하철 · 버스 · 도보로 나뉘고, 지하철은 노선명 · 버스는 번호")
	void parse_steps() {
		List<Route> routes = parser.parse(SUCCESS_JSON);

		assertThat(routes.get(0).steps()).containsExactly(
				new Step(StepType.WALK, null, null, null, 3),
				new Step(StepType.SUBWAY, "수도권 4호선", "서울역", "사당", 16),
				new Step(StepType.WALK, null, null, null, 2),
				new Step(StepType.SUBWAY, "수도권 2호선", "사당", "강남", 9),
				new Step(StepType.WALK, null, null, null, 1));
		// 한 구간에 탈 수 있는 버스가 여러 대면 쉼표로 이어 붙임
		assertThat(routes.get(1).steps().getFirst())
				.isEqualTo(new Step(StepType.BUS, "402, 405", "서울역버스환승센터(5번승강장)", "신분당선강남역", 34));
	}

	@Test
	@DisplayName("출발 · 도착지가 700m 이내(-98)면 빈 목록")
	void parse_tooClose() {
		assertThat(parser.parse(TOO_CLOSE_JSON)).isEmpty();
	}

	@Test
	@DisplayName("검색 결과가 없으면(-99) 빈 목록")
	void parse_noResult() {
		String json = """
				{"error":{"msg":"검색결과가 없습니다.","code":"-99"}}
				""";

		assertThat(parser.parse(json)).isEmpty();
	}

	@Test
	@DisplayName("키 인증 실패면 예외 (오류가 배열 모양으로 옴)")
	void parse_authFailed() {
		assertThatThrownBy(() -> parser.parse(AUTH_FAILED_JSON)).isInstanceOf(TransitApiException.class);
	}

	@Test
	@DisplayName("JSON이 아니거나 비어 있으면 예외")
	void parse_invalidJson() {
		assertThatThrownBy(() -> parser.parse("<html>Service Unavailable</html>"))
				.isInstanceOf(TransitApiException.class);
		assertThatThrownBy(() -> parser.parse(null))
				.isInstanceOf(TransitApiException.class);
		assertThatThrownBy(() -> parser.parse("{}"))
				.isInstanceOf(TransitApiException.class);
	}
}

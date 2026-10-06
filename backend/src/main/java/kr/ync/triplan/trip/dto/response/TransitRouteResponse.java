package kr.ync.triplan.trip.dto.response;

import java.util.List;

// 두 방문지 사이 대중교통 경로 후보 (소요시간이 짧은 순)
public record TransitRouteResponse(List<Route> routes) {

	public record Route(
			int totalTime,      // 분
			int payment,        // 원
			int transferCount,  // 환승 횟수
			int walkDistance,   // 총 도보 거리 (m)
			List<Step> steps
	) {
	}

	// 한 구간: 도보는 이름 · 출발 · 도착이 없다
	public record Step(
			StepType type,
			String name,  // 지하철 노선명(예: 수도권 4호선) 또는 버스 번호(여러 대면 쉼표로)
			String from,
			String to,
			int time      // 분
	) {
	}

	public enum StepType {
		SUBWAY, BUS, WALK
	}
}

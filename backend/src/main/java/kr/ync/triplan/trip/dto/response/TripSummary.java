package kr.ync.triplan.trip.dto.response;

import java.util.List;

// 목록 카드에 보여줄 여행 요약. region은 주소가 있는 방문지가 없으면 null
// route는 좌표가 있는 방문지를 일정 순서대로 (없으면 빈 목록)
public record TripSummary(
		String region,
		int stopCount,
		int totalCost,
		List<RoutePoint> route
) {
}

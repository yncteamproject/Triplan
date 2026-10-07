package kr.ync.triplan.trip.dto.response;

// 목록 카드에 보여줄 여행 요약. region은 주소가 있는 방문지가 없으면 null
public record TripSummary(
		String region,
		int stopCount,
		int totalCost
) {
}

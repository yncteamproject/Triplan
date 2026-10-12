package kr.ync.triplan.trip.dto.response;

// 여행 경로의 한 점 (방문지의 좌표). 목록 카드의 경로 그림에 쓴다
public record RoutePoint(
		double latitude,
		double longitude
) {
}

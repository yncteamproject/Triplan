package kr.ync.triplan.dto.response;

public record TripEstimateResponse(
		Long tripId,
		int transportCost,
		int lodgingCost,
		int totalCost
) {

	public static TripEstimateResponse of(Long tripId, int transportCost, int lodgingCost) {
		return new TripEstimateResponse(tripId, transportCost, lodgingCost, transportCost + lodgingCost);
	}
}

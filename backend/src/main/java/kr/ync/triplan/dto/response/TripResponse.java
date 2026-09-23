package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.Trip;

import java.time.LocalDate;

public record TripResponse(
		Long id,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		String userId
) {

	public static TripResponse from(Trip trip) {
		return new TripResponse(
				trip.getId(),
				trip.getTitle(),
				trip.getStartDate(),
				trip.getEndDate(),
				trip.getUserId()
		);
	}
}

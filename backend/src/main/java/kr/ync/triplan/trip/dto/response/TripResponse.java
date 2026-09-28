package kr.ync.triplan.trip.dto.response;

import kr.ync.triplan.trip.domain.Trip;

import java.time.LocalDate;

public record TripResponse(
		Long id,
		String title,
		LocalDate startDate,
		LocalDate endDate
) {

	public static TripResponse from(Trip trip) {
		return new TripResponse(
				trip.getId(),
				trip.getTitle(),
				trip.getStartDate(),
				trip.getEndDate()
		);
	}
}

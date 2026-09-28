package kr.ync.triplan.trip.dto.response;

import kr.ync.triplan.trip.domain.Lodging;

import java.time.LocalDateTime;

public record LodgingResponse(
		Long id,
		Long tripId,
		String name,
		LocalDateTime checkIn,
		LocalDateTime checkOut,
		Integer cost,
		String reservationNo
) {

	public static LodgingResponse from(Lodging lodging) {
		return new LodgingResponse(
				lodging.getId(),
				lodging.getTrip().getId(),
				lodging.getName(),
				lodging.getCheckIn(),
				lodging.getCheckOut(),
				lodging.getCost(),
				lodging.getReservationNo()
		);
	}
}

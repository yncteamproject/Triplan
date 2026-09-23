package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.Stop;

import java.time.LocalDate;
import java.time.LocalTime;

public record StopResponse(
		Long id,
		Long tripId,
		String name,
		LocalDate date,
		LocalTime time,
		String memo,
		String imageUrl,
		Integer stopOrder
) {

	public static StopResponse from(Stop stop) {
		return new StopResponse(
				stop.getId(),
				stop.getTrip().getId(),
				stop.getName(),
				stop.getDate(),
				stop.getTime(),
				stop.getMemo(),
				stop.getImageUrl(),
				stop.getStopOrder()
		);
	}
}

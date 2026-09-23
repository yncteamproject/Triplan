package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.TransportMode;
import kr.ync.triplan.domain.TransportSegment;

import java.time.LocalDateTime;

public record TransportSegmentResponse(
		Long id,
		Long tripId,
		Long fromStopId,
		Long toStopId,
		TransportMode mode,
		LocalDateTime departTime,
		LocalDateTime arriveTime,
		Integer cost,
		String reservationNo
) {

	public static TransportSegmentResponse from(TransportSegment segment) {
		return new TransportSegmentResponse(
				segment.getId(),
				segment.getTrip().getId(),
				segment.getFromStop().getId(),
				segment.getToStop().getId(),
				segment.getMode(),
				segment.getDepartTime(),
				segment.getArriveTime(),
				segment.getCost(),
				segment.getReservationNo()
		);
	}
}

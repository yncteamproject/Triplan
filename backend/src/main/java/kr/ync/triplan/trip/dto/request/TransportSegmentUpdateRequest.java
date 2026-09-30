package kr.ync.triplan.trip.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.ync.triplan.trip.domain.TransportMode;

import java.time.LocalDateTime;

public record TransportSegmentUpdateRequest(
		@NotNull(message = "출발지를 선택해주세요")
		Long fromStopId,

		@NotNull(message = "도착지를 선택해주세요")
		Long toStopId,

		@NotNull(message = "이동 수단을 선택해주세요")
		TransportMode mode,

		@NotNull(message = "출발 시간을 입력해주세요")
		LocalDateTime departTime,

		@NotNull(message = "도착 시간을 입력해주세요")
		LocalDateTime arriveTime,

		@PositiveOrZero(message = "비용은 0 이상이어야 합니다")
		Integer cost,

		String reservationNo
) {
	@AssertTrue(message = "도착 시간은 출발 시간보다 빠를 수 없습니다")
	public boolean isTimeRangeValid() {
		if (departTime == null || arriveTime == null) {
			return true;
		}
		return !arriveTime.isBefore(departTime);
	}
}

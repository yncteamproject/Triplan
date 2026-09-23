package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record LodgingUpdateRequest(
		@NotBlank(message = "숙소 이름을 입력해주세요")
		String name,

		@NotNull(message = "체크인 시간을 입력해주세요")
		LocalDateTime checkIn,

		@NotNull(message = "체크아웃 시간을 입력해주세요")
		LocalDateTime checkOut,

		@PositiveOrZero(message = "비용은 0 이상이어야 합니다")
		Integer cost,

		String reservationNo
) {
	@AssertTrue(message = "체크아웃 시간은 체크인 시간보다 빠를 수 없습니다")
	public boolean isStayRangeValid() {
		if (checkIn == null || checkOut == null) {
			return true;
		}
		return !checkOut.isBefore(checkIn);
	}
}

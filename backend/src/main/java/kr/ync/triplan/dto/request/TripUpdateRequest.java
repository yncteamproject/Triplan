package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TripUpdateRequest(
		@NotBlank(message = "여행 제목을 입력해주세요")
		String title,

		@NotNull(message = "시작일을 선택해주세요")
		LocalDate startDate,

		@NotNull(message = "종료일을 선택해주세요")
		LocalDate endDate
) {
	@AssertTrue(message = "종료일은 시작일보다 빠를 수 없습니다")
	public boolean isPeriodValid() {
		if (startDate == null || endDate == null) {
			return true;
		}
		return !endDate.isBefore(startDate);
	}
}

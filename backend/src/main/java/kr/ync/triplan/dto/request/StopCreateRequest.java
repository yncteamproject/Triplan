package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalTime;

public record StopCreateRequest(
		@NotBlank(message = "방문지 이름을 입력해주세요")
		String name,

		@NotNull(message = "방문 날짜를 선택해주세요")
		LocalDate date,

		LocalTime time,

		String memo,

		String imageUrl,

		@PositiveOrZero(message = "방문 순서는 0 이상이어야 합니다")
		Integer stopOrder
) {
}

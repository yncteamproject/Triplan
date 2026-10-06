package kr.ync.triplan.trip.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record StopUpdateRequest(
		@NotBlank(message = "방문지 이름을 입력해주세요")
		String name,

		@NotNull(message = "방문 날짜를 선택해주세요")
		LocalDate date,

		LocalTime time,

		String memo,

		String imageUrl,

		@PositiveOrZero(message = "방문 순서는 0 이상이어야 합니다")
		Integer stopOrder,

		@DecimalMin(value = "-90", message = "위도는 -90 ~ 90 사이여야 합니다")
		@DecimalMax(value = "90", message = "위도는 -90 ~ 90 사이여야 합니다")
		Double latitude,

		@DecimalMin(value = "-180", message = "경도는 -180 ~ 180 사이여야 합니다")
		@DecimalMax(value = "180", message = "경도는 -180 ~ 180 사이여야 합니다")
		Double longitude,

		@Size(max = 255, message = "주소는 255자 이내로 입력해주세요")
		String address
) {
	// 좌표는 지도에 찍을 수 있도록 위도 · 경도를 함께 보내야 한다
	@AssertTrue(message = "위도와 경도는 함께 입력해주세요")
	public boolean isCoordinateValid() {
		return (latitude == null) == (longitude == null);
	}
}

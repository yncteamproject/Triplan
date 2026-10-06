package kr.ync.triplan.share.dto.request;

import java.time.LocalDate;

public record TripCopyRequest(
		// 복사한 여행의 시작일. 안 보내면 원본 날짜 그대로
		LocalDate startDate
) {
}

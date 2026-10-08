package kr.ync.triplan.share.dto.response;

import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.trip.dto.response.TripSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SharePageListResponse(
		Long id,
		String title,
		Long writerId,
		String writerNickname,
		LocalDateTime writeDate,
		int viewCount,
		boolean allowCopy,
		int copyCount,
		// 목록 카드용 여행 요약 (S6). region은 주소가 있는 방문지가 없으면 null
		LocalDate tripStartDate,
		LocalDate tripEndDate,
		String region,
		int stopCount,
		int totalCost
) {

	public static SharePageListResponse of(SharePage sharePage, TripSummary summary) {
		return new SharePageListResponse(
				sharePage.getId(),
				sharePage.getTitle(),
				sharePage.getWriter().getId(),
				sharePage.getWriter().getNickname(),
				sharePage.getWriteDate(),
				sharePage.getViewCount(),
				sharePage.isAllowCopy(),
				sharePage.getCopyCount(),
				sharePage.getTrip().getStartDate(),
				sharePage.getTrip().getEndDate(),
				summary.region(),
				summary.stopCount(),
				summary.totalCost()
		);
	}
}

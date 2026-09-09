package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.SharePage;

import java.time.LocalDateTime;

public record SharePageResponse(
		Long id,
		String title,
		String description,
		Long tripId,
		String writerId,
		LocalDateTime writeDate,
		LocalDateTime updateDate,
		int viewCount
) {

	public static SharePageResponse from(SharePage sharePage) {
		return new SharePageResponse(
				sharePage.getId(),
				sharePage.getTitle(),
				sharePage.getDescription(),
				sharePage.getTrip().getId(),
				sharePage.getWriterId(),
				sharePage.getWriteDate(),
				sharePage.getUpdateDate(),
				sharePage.getViewCount()
		);
	}
}

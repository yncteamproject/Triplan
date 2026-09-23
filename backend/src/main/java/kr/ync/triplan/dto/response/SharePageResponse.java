package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.SharePage;

import java.time.LocalDateTime;

public record SharePageResponse(
		Long id,
		String title,
		String description,
		Long tripId,
		Long writerId,
		String writerNickname,
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
				sharePage.getWriter().getId(),
				sharePage.getWriter().getNickname(),
				sharePage.getWriteDate(),
				sharePage.getUpdateDate(),
				sharePage.getViewCount()
		);
	}
}

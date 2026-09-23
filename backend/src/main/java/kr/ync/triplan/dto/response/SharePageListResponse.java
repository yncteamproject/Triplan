package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.SharePage;

import java.time.LocalDateTime;

public record SharePageListResponse(
		Long id,
		String title,
		Long writerId,
		String writerNickname,
		LocalDateTime writeDate,
		int viewCount
) {

	public static SharePageListResponse from(SharePage sharePage) {
		return new SharePageListResponse(
				sharePage.getId(),
				sharePage.getTitle(),
				sharePage.getWriter().getId(),
				sharePage.getWriter().getNickname(),
				sharePage.getWriteDate(),
				sharePage.getViewCount()
		);
	}
}

package kr.ync.triplan.admin.dto.response;

import kr.ync.triplan.share.domain.SharePage;

import java.time.LocalDateTime;

public record AdminSharePageResponse(
		Long id,
		String title,
		String writerNickname,
		String writerEmail,
		LocalDateTime writeDate,
		int viewCount,
		int copyCount
) {
	public static AdminSharePageResponse from(SharePage sharePage) {
		return new AdminSharePageResponse(
				sharePage.getId(),
				sharePage.getTitle(),
				sharePage.getWriter().getNickname(),
				sharePage.getWriter().getEmail(),
				sharePage.getWriteDate(),
				sharePage.getViewCount(),
				sharePage.getCopyCount()
		);
	}
}

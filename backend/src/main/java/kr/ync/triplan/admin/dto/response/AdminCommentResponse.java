package kr.ync.triplan.admin.dto.response;

import kr.ync.triplan.share.domain.Comment;

import java.time.LocalDateTime;

public record AdminCommentResponse(
		Long id,
		String content,
		String writerNickname,
		Long sharePageId,
		String sharePageTitle,
		LocalDateTime createdAt
) {
	public static AdminCommentResponse from(Comment comment) {
		return new AdminCommentResponse(
				comment.getId(),
				comment.getContent(),
				comment.getWriter().getNickname(),
				comment.getSharePage().getId(),
				comment.getSharePage().getTitle(),
				comment.getCreatedAt()
		);
	}
}

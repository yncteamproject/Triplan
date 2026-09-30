package kr.ync.triplan.share.dto.response;

import kr.ync.triplan.share.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
		Long id,
		String content,
		Long writerId,
		String writerNickname,
		LocalDateTime createdAt
) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getContent(),
				comment.getWriter().getId(),
				comment.getWriter().getNickname(),
				comment.getCreatedAt()
		);
	}
}

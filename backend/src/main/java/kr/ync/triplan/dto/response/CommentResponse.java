package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
		Long id,
		String content,
		String writerId,
		LocalDateTime createdAt
) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getContent(),
				comment.getWriterId(),
				comment.getCreatedAt()
		);
	}
}

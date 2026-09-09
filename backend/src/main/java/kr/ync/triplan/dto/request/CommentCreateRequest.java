package kr.ync.triplan.dto.request;

public record CommentCreateRequest(
		String content,
		String writerId
) {
}

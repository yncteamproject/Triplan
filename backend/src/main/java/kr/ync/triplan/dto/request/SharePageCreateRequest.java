package kr.ync.triplan.dto.request;

public record SharePageCreateRequest(
		String title,
		String description,
		Long tripId,
		String writerId
) {
}

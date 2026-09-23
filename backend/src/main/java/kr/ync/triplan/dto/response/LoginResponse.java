package kr.ync.triplan.dto.response;

public record LoginResponse(
		String accessToken,
		Long memberId,
		String nickname
) {
}

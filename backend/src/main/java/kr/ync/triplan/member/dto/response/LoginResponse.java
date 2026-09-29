package kr.ync.triplan.member.dto.response;

public record LoginResponse(
		String accessToken,
		Long memberId,
		String nickname
) {
}

package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.Member;

public record MemberResponse(
		Long id,
		String email,
		String nickname
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getEmail(), member.getNickname());
	}
}

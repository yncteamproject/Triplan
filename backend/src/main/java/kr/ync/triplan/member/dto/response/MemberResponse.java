package kr.ync.triplan.member.dto.response;

import kr.ync.triplan.member.domain.Member;

public record MemberResponse(
		Long id,
		String email,
		String nickname
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getEmail(), member.getNickname());
	}
}

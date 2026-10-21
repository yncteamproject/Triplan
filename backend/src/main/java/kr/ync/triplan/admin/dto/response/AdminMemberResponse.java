package kr.ync.triplan.admin.dto.response;

import kr.ync.triplan.member.domain.Member;

import java.time.LocalDateTime;

public record AdminMemberResponse(
		Long id,
		String email,
		String nickname,
		String role,
		LocalDateTime createdAt
) {
	public static AdminMemberResponse from(Member member) {
		return new AdminMemberResponse(
				member.getId(),
				member.getEmail(),
				member.getNickname(),
				member.getRole().name(),
				member.getCreatedAt()
		);
	}
}

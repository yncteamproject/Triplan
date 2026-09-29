package kr.ync.triplan.member.dto.response;

import kr.ync.triplan.member.domain.Member;

import java.time.LocalDateTime;

// 마이페이지 응답: 공통 MemberResponse(id, email, nickname)에 권한/가입일 추가
public record MyPageResponse(
		Long id,
		String email,
		String nickname,
		String role,
		LocalDateTime createdAt
) {
	public static MyPageResponse from(Member member) {
		return new MyPageResponse(
				member.getId(),
				member.getEmail(),
				member.getNickname(),
				member.getRole().name(),
				member.getCreatedAt()
		);
	}
}

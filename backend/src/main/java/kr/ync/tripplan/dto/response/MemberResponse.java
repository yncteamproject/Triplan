package kr.ync.tripplan.dto.response;

import kr.ync.tripplan.domain.Member;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String role,
        LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}
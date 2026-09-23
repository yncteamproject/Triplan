package kr.ync.tripplan.dto.request;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        String nickname,
        @Size(min = 8, max = 20)
        String password
) {
}

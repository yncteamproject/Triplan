package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(max = 20, message = "닉네임은 20자 이내로 입력해주세요")
        String nickname,

        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        String password
) {}
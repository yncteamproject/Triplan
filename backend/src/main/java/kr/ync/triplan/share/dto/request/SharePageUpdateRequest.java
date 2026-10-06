package kr.ync.triplan.share.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SharePageUpdateRequest(
		@NotBlank(message = "제목을 입력해주세요")
		@Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
		String title,

		@Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요")
		String description,

		// 복사 허용 여부. 안 보내면 기존 값 유지
		Boolean allowCopy
) {
}

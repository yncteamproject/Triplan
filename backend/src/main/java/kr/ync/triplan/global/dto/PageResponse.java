package kr.ync.triplan.global.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// 목록을 페이지 단위로 반환할 때 쓰는 공통 응답. Spring의 Page를 그대로 내보내지 않고 필요한 값만 담는다
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast()
		);
	}
}

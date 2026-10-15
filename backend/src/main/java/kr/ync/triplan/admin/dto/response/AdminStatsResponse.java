package kr.ync.triplan.admin.dto.response;


public record AdminStatsResponse(
		long memberCount,
		long sharePageCount,
		long commentCount
) {
}

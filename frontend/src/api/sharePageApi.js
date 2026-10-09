import { apiClient } from "./client";

// 게시글 목록 (로그인 없이 조회 가능). page는 0부터, 최신 글부터 온다
// 응답: { content, page, size, totalElements, totalPages, last }
export const getSharePages = (page, size) =>
	apiClient.get("/api/share-pages", { params: { page, size } }).then((res) => res.data);

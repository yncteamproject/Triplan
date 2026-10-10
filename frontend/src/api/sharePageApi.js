import { apiClient } from "./client";

// 게시글 목록 (로그인 없이 조회 가능). page는 0부터, 최신 글부터 온다
// 응답: { content, page, size, totalElements, totalPages, last }
export const getSharePages = (page, size) =>
	apiClient.get("/api/share-pages", { params: { page, size } }).then((res) => res.data);

// 게시글 상세 (로그인 없이 조회 가능). 부를 때마다 조회수가 1 오른다
export const getSharePage = (id) =>
	apiClient.get(`/api/share-pages/${id}`).then((res) => res.data);

// 게시글에 달린 댓글도 함께 지워진다
export const deleteSharePage = (id) => apiClient.delete(`/api/share-pages/${id}`);

// 게시글에 연결된 여행의 방문지 · 이동 구간 · 숙소 · 경비 (로그인 없이 조회 가능, 예약번호는 오지 않는다)
export const getSharedTrip = (id) =>
	apiClient.get(`/api/share-pages/${id}/trip`).then((res) => res.data);

// 공유된 여행을 내 여행으로 복사한다. startDate를 주면 그 날짜에 시작하도록 모든 날짜가 옮겨진다
// 응답: 새로 만들어진 내 여행 { id, title, startDate, endDate }
export const copySharedTrip = (id, startDate) =>
	apiClient.post(`/api/share-pages/${id}/copy`, startDate ? { startDate } : {}).then((res) => res.data);

// 댓글 (목록은 로그인 없이 조회 가능, 등록순)
export const getComments = (sharePageId) =>
	apiClient.get(`/api/share-pages/${sharePageId}/comments`).then((res) => res.data);

export const createComment = (sharePageId, content) =>
	apiClient.post(`/api/share-pages/${sharePageId}/comments`, { content }).then((res) => res.data);

export const deleteComment = (commentId) => apiClient.delete(`/api/comments/${commentId}`);

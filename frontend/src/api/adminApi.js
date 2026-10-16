import { apiClient } from "./client";

// 관리자 전용. 관리자가 아니면 403
// 목록 응답: { content, page, size, totalElements, totalPages, last }, page는 0부터
export const getAdminStats = () =>
	apiClient.get("/api/admin/stats").then((res) => res.data);

export const getAdminSharePages = (page, size) =>
	apiClient.get("/api/admin/share-pages", { params: { page, size } }).then((res) => res.data);

// 달린 댓글도 함께 지워짐
export const deleteAdminSharePage = (id) => apiClient.delete(`/api/admin/share-pages/${id}`);

export const getAdminComments = (page, size) =>
	apiClient.get("/api/admin/comments", { params: { page, size } }).then((res) => res.data);

export const deleteAdminComment = (id) => apiClient.delete(`/api/admin/comments/${id}`);

export const getAdminMembers = (page, size) =>
	apiClient.get("/api/admin/members", { params: { page, size } }).then((res) => res.data);
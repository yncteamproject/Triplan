package kr.ync.triplan.admin.controller;

import kr.ync.triplan.admin.dto.response.AdminCommentResponse;
import kr.ync.triplan.admin.dto.response.AdminMemberResponse;
import kr.ync.triplan.admin.dto.response.AdminSharePageResponse;
import kr.ync.triplan.admin.dto.response.AdminStatsResponse;
import kr.ync.triplan.admin.service.AdminService;
import kr.ync.triplan.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 API. 관리자인지는 AdminService에서 확인
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	@GetMapping("/stats")
	public ResponseEntity<AdminStatsResponse> getStats(@AuthenticationPrincipal String email) {
		return ResponseEntity.ok(adminService.getStats(email));
	}

	@GetMapping("/share-pages")
	public ResponseEntity<PageResponse<AdminSharePageResponse>> getSharePages(
			@AuthenticationPrincipal String email,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(adminService.getSharePages(email, page, size));
	}

	@DeleteMapping("/share-pages/{id}")
	public ResponseEntity<Void> deleteSharePage(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		adminService.deleteSharePage(email, id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/comments")
	public ResponseEntity<PageResponse<AdminCommentResponse>> getComments(
			@AuthenticationPrincipal String email,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(adminService.getComments(email, page, size));
	}

	@DeleteMapping("/comments/{id}")
	public ResponseEntity<Void> deleteComment(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		adminService.deleteComment(email, id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/members")
	public ResponseEntity<PageResponse<AdminMemberResponse>> getMembers(
			@AuthenticationPrincipal String email,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(adminService.getMembers(email, page, size));
	}
}
package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.dto.response.CommentResponse;
import kr.ync.triplan.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/api/share-pages/{sharePageId}/comments")
	public ResponseEntity<CommentResponse> create(
			@AuthenticationPrincipal String email, @PathVariable Long sharePageId,
			@Valid @RequestBody CommentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(email, sharePageId, request));
	}

	@GetMapping("/api/share-pages/{sharePageId}/comments")
	public ResponseEntity<List<CommentResponse>> getList(@PathVariable Long sharePageId) {
		return ResponseEntity.ok(commentService.getList(sharePageId));
	}

	@DeleteMapping("/api/comments/{commentId}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long commentId) {
		commentService.delete(email, commentId);
		return ResponseEntity.noContent().build();
	}
}

package kr.ync.triplan.controller;

import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.dto.response.CommentResponse;
import kr.ync.triplan.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
			@PathVariable Long sharePageId, @Valid @RequestBody CommentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(sharePageId, request));
	}

	@GetMapping("/api/share-pages/{sharePageId}/comments")
	public ResponseEntity<List<CommentResponse>> getList(
			@PathVariable Long sharePageId) {
		return ResponseEntity.ok(commentService.getList(sharePageId));
	}

	@DeleteMapping("/api/comments/{commentId}")
	public ResponseEntity<Void> delete(
			@PathVariable Long commentId) {
		commentService.delete(commentId);
		return ResponseEntity.noContent().build();
	}
}

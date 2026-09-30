package kr.ync.triplan.share.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.service.SharePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/share-pages")
@RequiredArgsConstructor
public class SharePageController {

	private final SharePageService sharePageService;

	@PostMapping
	public ResponseEntity<SharePageResponse> create(
			@AuthenticationPrincipal String email, @Valid @RequestBody SharePageCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(sharePageService.create(email, request));
	}

	@GetMapping
	public ResponseEntity<List<SharePageListResponse>> getList() {
		return ResponseEntity.ok(sharePageService.getList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<SharePageResponse> getDetail(@PathVariable Long id) {
		return ResponseEntity.ok(sharePageService.getDetail(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<SharePageResponse> update(
			@AuthenticationPrincipal String email, @PathVariable Long id,
			@Valid @RequestBody SharePageUpdateRequest request) {
		return ResponseEntity.ok(sharePageService.update(email, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		sharePageService.delete(email, id);
		return ResponseEntity.noContent().build();
	}
}

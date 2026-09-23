package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.StopCreateRequest;
import kr.ync.triplan.dto.request.StopUpdateRequest;
import kr.ync.triplan.dto.response.StopResponse;
import kr.ync.triplan.service.StopService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StopController {

	private final StopService stopService;

	@PostMapping("/api/trips/{tripId}/stops")
	public ResponseEntity<StopResponse> create(
			@AuthenticationPrincipal String email, @PathVariable Long tripId,
			@Valid @RequestBody StopCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(stopService.create(email, tripId, request));
	}

	@GetMapping("/api/trips/{tripId}/stops")
	public ResponseEntity<List<StopResponse>> getList(
			@AuthenticationPrincipal String email, @PathVariable Long tripId) {
		return ResponseEntity.ok(stopService.getList(email, tripId));
	}

	@GetMapping("/api/stops/{id}")
	public ResponseEntity<StopResponse> getDetail(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		return ResponseEntity.ok(stopService.getDetail(email, id));
	}

	@PutMapping("/api/stops/{id}")
	public ResponseEntity<StopResponse> update(
			@AuthenticationPrincipal String email, @PathVariable Long id,
			@Valid @RequestBody StopUpdateRequest request) {
		return ResponseEntity.ok(stopService.update(email, id, request));
	}

	@DeleteMapping("/api/stops/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		stopService.delete(email, id);
		return ResponseEntity.noContent().build();
	}
}

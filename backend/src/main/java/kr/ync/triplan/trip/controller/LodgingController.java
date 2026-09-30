package kr.ync.triplan.trip.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.trip.dto.request.LodgingCreateRequest;
import kr.ync.triplan.trip.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.trip.dto.response.LodgingResponse;
import kr.ync.triplan.trip.service.LodgingService;
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
public class LodgingController {

	private final LodgingService lodgingService;

	@PostMapping("/api/trips/{tripId}/lodgings")
	public ResponseEntity<LodgingResponse> create(
			@AuthenticationPrincipal String email, @PathVariable Long tripId,
			@Valid @RequestBody LodgingCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(lodgingService.create(email, tripId, request));
	}

	@GetMapping("/api/trips/{tripId}/lodgings")
	public ResponseEntity<List<LodgingResponse>> getList(
			@AuthenticationPrincipal String email, @PathVariable Long tripId) {
		return ResponseEntity.ok(lodgingService.getList(email, tripId));
	}

	@GetMapping("/api/lodgings/{id}")
	public ResponseEntity<LodgingResponse> getDetail(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		return ResponseEntity.ok(lodgingService.getDetail(email, id));
	}

	@PutMapping("/api/lodgings/{id}")
	public ResponseEntity<LodgingResponse> update(
			@AuthenticationPrincipal String email, @PathVariable Long id,
			@Valid @RequestBody LodgingUpdateRequest request) {
		return ResponseEntity.ok(lodgingService.update(email, id, request));
	}

	@DeleteMapping("/api/lodgings/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		lodgingService.delete(email, id);
		return ResponseEntity.noContent().build();
	}
}

package kr.ync.triplan.trip.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.trip.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.trip.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.trip.dto.response.TransportSegmentResponse;
import kr.ync.triplan.trip.service.TransportSegmentService;
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
public class TransportSegmentController {

	private final TransportSegmentService transportSegmentService;

	@PostMapping("/api/trips/{tripId}/transport-segments")
	public ResponseEntity<TransportSegmentResponse> create(
			@AuthenticationPrincipal String email, @PathVariable Long tripId,
			@Valid @RequestBody TransportSegmentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(transportSegmentService.create(email, tripId, request));
	}

	@GetMapping("/api/trips/{tripId}/transport-segments")
	public ResponseEntity<List<TransportSegmentResponse>> getList(
			@AuthenticationPrincipal String email, @PathVariable Long tripId) {
		return ResponseEntity.ok(transportSegmentService.getList(email, tripId));
	}

	@GetMapping("/api/transport-segments/{id}")
	public ResponseEntity<TransportSegmentResponse> getDetail(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		return ResponseEntity.ok(transportSegmentService.getDetail(email, id));
	}

	@PutMapping("/api/transport-segments/{id}")
	public ResponseEntity<TransportSegmentResponse> update(
			@AuthenticationPrincipal String email, @PathVariable Long id,
			@Valid @RequestBody TransportSegmentUpdateRequest request) {
		return ResponseEntity.ok(transportSegmentService.update(email, id, request));
	}

	@DeleteMapping("/api/transport-segments/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		transportSegmentService.delete(email, id);
		return ResponseEntity.noContent().build();
	}
}

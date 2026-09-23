package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.dto.response.TransportSegmentResponse;
import kr.ync.triplan.service.TransportSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
			@PathVariable Long tripId, @Valid @RequestBody TransportSegmentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transportSegmentService.create(tripId, request));
	}

	@GetMapping("/api/trips/{tripId}/transport-segments")
	public ResponseEntity<List<TransportSegmentResponse>> getList(@PathVariable Long tripId) {
		return ResponseEntity.ok(transportSegmentService.getList(tripId));
	}

	@GetMapping("/api/transport-segments/{id}")
	public ResponseEntity<TransportSegmentResponse> getDetail(@PathVariable Long id) {
		return ResponseEntity.ok(transportSegmentService.getDetail(id));
	}

	@PutMapping("/api/transport-segments/{id}")
	public ResponseEntity<TransportSegmentResponse> update(
			@PathVariable Long id, @Valid @RequestBody TransportSegmentUpdateRequest request) {
		return ResponseEntity.ok(transportSegmentService.update(id, request));
	}

	@DeleteMapping("/api/transport-segments/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		transportSegmentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

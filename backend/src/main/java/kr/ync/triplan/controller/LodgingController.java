package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.LodgingCreateRequest;
import kr.ync.triplan.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.dto.response.LodgingResponse;
import kr.ync.triplan.service.LodgingService;
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
public class LodgingController {

	private final LodgingService lodgingService;

	@PostMapping("/api/trips/{tripId}/lodgings")
	public ResponseEntity<LodgingResponse> create(
			@PathVariable Long tripId, @Valid @RequestBody LodgingCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(lodgingService.create(tripId, request));
	}

	@GetMapping("/api/trips/{tripId}/lodgings")
	public ResponseEntity<List<LodgingResponse>> getList(@PathVariable Long tripId) {
		return ResponseEntity.ok(lodgingService.getList(tripId));
	}

	@GetMapping("/api/lodgings/{id}")
	public ResponseEntity<LodgingResponse> getDetail(@PathVariable Long id) {
		return ResponseEntity.ok(lodgingService.getDetail(id));
	}

	@PutMapping("/api/lodgings/{id}")
	public ResponseEntity<LodgingResponse> update(
			@PathVariable Long id, @Valid @RequestBody LodgingUpdateRequest request) {
		return ResponseEntity.ok(lodgingService.update(id, request));
	}

	@DeleteMapping("/api/lodgings/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		lodgingService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

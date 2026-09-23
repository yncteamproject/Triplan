package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripEstimateResponse;
import kr.ync.triplan.dto.response.TripResponse;
import kr.ync.triplan.service.TripService;
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
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	@PostMapping
	public ResponseEntity<TripResponse> create(
			@AuthenticationPrincipal String email, @Valid @RequestBody TripCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tripService.create(email, request));
	}

	@GetMapping
	public ResponseEntity<List<TripResponse>> getList(@AuthenticationPrincipal String email) {
		return ResponseEntity.ok(tripService.getList(email));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TripResponse> getDetail(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		return ResponseEntity.ok(tripService.getDetail(email, id));
	}

	@GetMapping("/{id}/estimate")
	public ResponseEntity<TripEstimateResponse> getEstimate(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		return ResponseEntity.ok(tripService.getEstimate(email, id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TripResponse> update(
			@AuthenticationPrincipal String email, @PathVariable Long id,
			@Valid @RequestBody TripUpdateRequest request) {
		return ResponseEntity.ok(tripService.update(email, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal String email, @PathVariable Long id) {
		tripService.delete(email, id);
		return ResponseEntity.noContent().build();
	}
}

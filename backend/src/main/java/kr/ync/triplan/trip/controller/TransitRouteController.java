package kr.ync.triplan.trip.controller;

import kr.ync.triplan.trip.dto.response.TransitRouteResponse;
import kr.ync.triplan.trip.service.TransitRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransitRouteController {

	private final TransitRouteService transitRouteService;

	@GetMapping("/api/trips/{tripId}/transit-routes")
	public ResponseEntity<TransitRouteResponse> getRoutes(
			@AuthenticationPrincipal String email, @PathVariable Long tripId,
			@RequestParam Long fromStopId, @RequestParam Long toStopId) {
		return ResponseEntity.ok(transitRouteService.getRoutes(email, tripId, fromStopId, toStopId));
	}
}

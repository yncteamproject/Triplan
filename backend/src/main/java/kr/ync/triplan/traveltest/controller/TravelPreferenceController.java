package kr.ync.triplan.traveltest.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.traveltest.dto.response.MyTravelResultResponse;
import kr.ync.triplan.traveltest.dto.response.TravelTestResultResponse;
import kr.ync.triplan.traveltest.dto.request.TravelTestSubmitRequest;
import kr.ync.triplan.traveltest.service.TravelPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel-test")
@RequiredArgsConstructor
public class TravelPreferenceController {

    private final TravelPreferenceService travelPreferenceService;

    @PostMapping
    public ResponseEntity<TravelTestResultResponse> submit(
            Authentication authentication,
            @Valid @RequestBody TravelTestSubmitRequest request) {
        return ResponseEntity.ok(travelPreferenceService.submit(authentication.getName(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<MyTravelResultResponse> getMyResult(Authentication authentication) {
        return ResponseEntity.ok(travelPreferenceService.getMyResult(authentication.getName()));
    }
}
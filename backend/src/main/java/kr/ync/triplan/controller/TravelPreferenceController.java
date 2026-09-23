package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.response.TravelTestResultResponse;
import kr.ync.triplan.dto.request.TravelTestSubmitRequest;
import kr.ync.triplan.service.TravelPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
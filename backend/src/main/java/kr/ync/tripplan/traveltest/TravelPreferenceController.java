package kr.ync.tripplan.traveltest;

import jakarta.validation.Valid;
import kr.ync.tripplan.traveltest.dto.TravelTestResultResponse;
import kr.ync.tripplan.traveltest.dto.TravelTestSubmitRequest;
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
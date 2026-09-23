package kr.ync.triplan.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.dto.request.MemberUpdateRequest;
import kr.ync.triplan.dto.response.MemberResponse;
import kr.ync.triplan.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(Authentication authentication) {
        return ResponseEntity.ok(memberService.getMyInfo(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.ok(memberService.updateMyInfo(authentication.getName(), request));
    }
}

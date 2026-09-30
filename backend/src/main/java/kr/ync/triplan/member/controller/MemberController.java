package kr.ync.triplan.member.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.member.dto.request.MemberUpdateRequest;
import kr.ync.triplan.member.dto.response.MyPageResponse;
import kr.ync.triplan.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MyPageService myPageService;

    @GetMapping("/me")
    public ResponseEntity<MyPageResponse> getMyInfo(Authentication authentication) {
        return ResponseEntity.ok(myPageService.getMyInfo(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<MyPageResponse> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.ok(myPageService.updateMyInfo(authentication.getName(), request));
    }
}

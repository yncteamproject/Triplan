package kr.ync.tripplan.controller;

import jakarta.validation.Valid;
import kr.ync.tripplan.dto.request.MemberUpdateRequest;
import kr.ync.tripplan.dto.response.MemberResponse;
import kr.ync.tripplan.service.MemberService;
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
    public ResponseEntity<MemberResponse> getInfo(Authentication authentication){
        return ResponseEntity.ok(memberService.getMyInfo(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateInfo(Authentication authentication,
                                                     @Valid
                                                     @RequestBody MemberUpdateRequest request){
        return ResponseEntity.ok(memberService.getMyInfo(authentication.getName()));
    }
}

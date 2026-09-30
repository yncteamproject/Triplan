package kr.ync.triplan.member.controller;

import jakarta.validation.Valid;
import kr.ync.triplan.member.dto.request.LoginRequest;
import kr.ync.triplan.member.dto.request.SignupRequest;
import kr.ync.triplan.member.dto.response.LoginResponse;
import kr.ync.triplan.member.dto.response.MemberResponse;
import kr.ync.triplan.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final MemberService memberService;

	@PostMapping("/signup")
	public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signup(request));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(memberService.login(request));
	}
}

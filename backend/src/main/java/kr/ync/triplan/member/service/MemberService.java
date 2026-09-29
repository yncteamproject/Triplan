package kr.ync.triplan.member.service;

import kr.ync.triplan.member.dto.request.LoginRequest;
import kr.ync.triplan.member.dto.request.SignupRequest;
import kr.ync.triplan.member.dto.response.LoginResponse;
import kr.ync.triplan.member.dto.response.MemberResponse;

public interface MemberService {

	MemberResponse signup(SignupRequest request);

	LoginResponse login(LoginRequest request);
}

package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.LoginRequest;
import kr.ync.triplan.dto.request.SignupRequest;
import kr.ync.triplan.dto.response.LoginResponse;
import kr.ync.triplan.dto.response.MemberResponse;

public interface MemberService {

	MemberResponse signup(SignupRequest request);

	LoginResponse login(LoginRequest request);
}

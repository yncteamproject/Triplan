package kr.ync.triplan.member.service;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.dto.request.MemberUpdateRequest;
import kr.ync.triplan.member.dto.response.MyPageResponse;
import kr.ync.triplan.member.exception.MemberNotFoundException;
import kr.ync.triplan.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MyPageResponse getMyInfo(String email) {
		return MyPageResponse.from(findMember(email));
	}

	@Transactional
	public MyPageResponse updateMyInfo(String email, MemberUpdateRequest request) {
		Member member = findMember(email);

		// 비워서 보낸 항목은 기존 값 유지
		if (request.nickname() != null && !request.nickname().isBlank()) {
			member.changeNickname(request.nickname());
		}
		if (request.password() != null && !request.password().isBlank()) {
			member.changePassword(passwordEncoder.encode(request.password()));
		}

		return MyPageResponse.from(member);
	}

	private Member findMember(String email) {
		return memberRepository.findByEmail(email)
				.orElseThrow(MemberNotFoundException::new);
	}
}

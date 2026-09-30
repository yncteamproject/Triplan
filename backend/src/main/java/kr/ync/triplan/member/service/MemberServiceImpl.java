package kr.ync.triplan.member.service;

import kr.ync.triplan.global.jwt.JwtTokenProvider;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.dto.request.LoginRequest;
import kr.ync.triplan.member.dto.request.SignupRequest;
import kr.ync.triplan.member.dto.response.LoginResponse;
import kr.ync.triplan.member.dto.response.MemberResponse;
import kr.ync.triplan.member.exception.DuplicateEmailException;
import kr.ync.triplan.member.exception.InvalidCredentialsException;
import kr.ync.triplan.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	@Transactional
	public MemberResponse signup(SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new DuplicateEmailException();
		}

		Member member = Member.builder()
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.nickname(request.nickname())
				.build();

		return MemberResponse.from(memberRepository.save(member));
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new InvalidCredentialsException();
		}

		String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
		return new LoginResponse(token, member.getId(), member.getNickname());
	}
}

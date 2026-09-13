package kr.ync.tripplan.member;

import kr.ync.tripplan.global.jwt.JwtTokenProvider;
import kr.ync.tripplan.member.dto.LoginRequest;
import kr.ync.tripplan.member.dto.LoginResponse;
import kr.ync.tripplan.member.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        return memberRepository.save(member).getId();
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
        return new LoginResponse(token, member.getNickname());
    }
}
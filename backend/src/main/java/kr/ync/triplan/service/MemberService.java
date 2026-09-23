package kr.ync.triplan.service;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.dto.request.MemberUpdateRequest;
import kr.ync.triplan.dto.response.MemberResponse;
import kr.ync.triplan.exception.DuplicateEmailException;
import kr.ync.triplan.exception.LoginFailedException;
import kr.ync.triplan.exception.MemberNotFoundException;
import kr.ync.triplan.jwt.JwtTokenProvider;
import kr.ync.triplan.dto.request.LoginRequest;
import kr.ync.triplan.dto.response.LoginResponse;
import kr.ync.triplan.dto.request.SignupRequest;
import kr.ync.triplan.repository.MemberRepository;
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
            throw new DuplicateEmailException();
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
                .orElseThrow(LoginFailedException::new);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new LoginFailedException();
        }
        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
        return new LoginResponse(token, member.getNickname());
    }

    public MemberResponse getMyInfo(String email){
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMyInfo(String email, MemberUpdateRequest request){
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        member.updateNickname(request.nickname());
        if(request.password() != null && !request.password().isBlank()){
            member.updatePassword(passwordEncoder.encode(request.password()));
        }

        return MemberResponse.from(member);
    }

}
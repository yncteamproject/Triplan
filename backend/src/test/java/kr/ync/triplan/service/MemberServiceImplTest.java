package kr.ync.triplan.service;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.dto.request.LoginRequest;
import kr.ync.triplan.dto.request.SignupRequest;
import kr.ync.triplan.dto.response.LoginResponse;
import kr.ync.triplan.dto.response.MemberResponse;
import kr.ync.triplan.exception.DuplicateEmailException;
import kr.ync.triplan.exception.InvalidCredentialsException;
import kr.ync.triplan.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberServiceImplTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Member savedMember(String email, String rawPassword) {
		return memberRepository.save(
				Member.builder()
						.email(email)
						.password(passwordEncoder.encode(rawPassword))
						.nickname("홍길동")
						.build()
		);
	}

	@Test
	@DisplayName("signup")
	void signup_success() {
		// given
		SignupRequest request = new SignupRequest("hong@test.com", "password123", "홍길동");
		// when
		MemberResponse response = memberService.signup(request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(MemberResponse::email, MemberResponse::nickname)
				.containsExactly("hong@test.com", "홍길동");
	}

	@Test
	@DisplayName("signup - 비밀번호는 암호화되어 저장됨")
	void signup_passwordEncoded() {
		// given
		SignupRequest request = new SignupRequest("hong@test.com", "password123", "홍길동");
		// when
		memberService.signup(request);
		// then
		Member saved = memberRepository.findByEmail("hong@test.com").orElseThrow();
		assertThat(saved.getPassword()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
	}

	@Test
	@DisplayName("signup - 중복 이메일이면 예외")
	void signup_duplicateEmail() {
		// given
		savedMember("hong@test.com", "password123");
		SignupRequest request = new SignupRequest("hong@test.com", "password456", "다른닉네임");
		// when & then
		assertThatThrownBy(() -> memberService.signup(request))
				.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	@DisplayName("login")
	void login_success() {
		// given
		savedMember("hong@test.com", "password123");
		LoginRequest request = new LoginRequest("hong@test.com", "password123");
		// when
		LoginResponse response = memberService.login(request);
		// then
		assertThat(response.accessToken()).isNotBlank();
		assertThat(response.nickname()).isEqualTo("홍길동");
	}

	@Test
	@DisplayName("login - 존재하지 않는 이메일이면 예외")
	void login_emailNotFound() {
		LoginRequest request = new LoginRequest("noSuchEmail@test.com", "password123");
		assertThatThrownBy(() -> memberService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	@DisplayName("login - 비밀번호가 틀리면 예외")
	void login_wrongPassword() {
		// given
		savedMember("hong@test.com", "password123");
		LoginRequest request = new LoginRequest("hong@test.com", "wrongPassword");
		// when & then
		assertThatThrownBy(() -> memberService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}
}

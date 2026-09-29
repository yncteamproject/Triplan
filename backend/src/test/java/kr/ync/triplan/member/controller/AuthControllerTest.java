package kr.ync.triplan.member.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.dto.request.LoginRequest;
import kr.ync.triplan.member.dto.request.SignupRequest;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.support.BaseController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseController {

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
	@DisplayName("POST /api/auth/signup - 1.정상 데이터")
	void signup_endpoint_validData() throws Exception {
		SignupRequest request = new SignupRequest("hong@test.com", "password123", "홍길동");

		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value("hong@test.com"))
				.andExpect(jsonPath("$.nickname").value("홍길동"));
	}

	@Test
	@DisplayName("POST /api/auth/signup - 2.필수 데이터 누락 (email 키 자체 없음)")
	void signup_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "password": "password123",
				  "nickname": "홍길동"
				}
				""";

		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("이메일을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/auth/signup - 3.null 값 (모든 필드 null)")
	void signup_endpoint_nullValues() throws Exception {
		SignupRequest request = new SignupRequest(null, null, null);

		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/auth/signup - 4.비정상 데이터 (비밀번호 8자 미만)")
	void signup_endpoint_invalidData() throws Exception {
		SignupRequest request = new SignupRequest("hong@test.com", "1234", "홍길동");

		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/auth/signup - 이미 가입된 이메일이면 409")
	void signup_endpoint_duplicateEmail() throws Exception {
		savedMember("hong@test.com", "password123");
		SignupRequest request = new SignupRequest("hong@test.com", "password456", "다른닉네임");

		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
	}

	@Test
	@DisplayName("POST /api/auth/login - 성공시 토큰 발급")
	void login_endpoint_success() throws Exception {
		savedMember("hong@test.com", "password123");
		LoginRequest request = new LoginRequest("hong@test.com", "password123");

		mockMvc.perform(
						post("/api/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.nickname").value("홍길동"));
	}

	@Test
	@DisplayName("POST /api/auth/login - 비밀번호가 틀리면 401")
	void login_endpoint_invalidCredentials() throws Exception {
		savedMember("hong@test.com", "password123");
		LoginRequest request = new LoginRequest("hong@test.com", "wrongPassword");

		mockMvc.perform(
						post("/api/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
	}
}

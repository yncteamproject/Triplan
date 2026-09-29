package kr.ync.triplan.support;

import kr.ync.triplan.global.jwt.JwtTokenProvider;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Transactional
public abstract class BaseController {

	@Autowired
	protected WebApplicationContext webApplicationContext;
	// Spring Web 애플리케이션 컨텍스트

	protected MockMvc mockMvc;
	// Controller 테스트를 위한 MockMVC 객체

	@Autowired
	protected ObjectMapper objectMapper;
	// JSON 직렬화 및 역직렬화

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	protected Member member;
	// 로그인한 기본 사용자

	@BeforeEach
	protected void setUpMockMvc() {
		// Spring MVC 처럼 test환경의 MVC (Security 필터까지 적용)
		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(webApplicationContext)
						.apply(springSecurity())
						.build();
		this.member = createMember("tester@test.com");
	}

	protected Member createMember(String email) {
		return memberRepository.save(
				Member.builder()
						.email(email)
						.password(passwordEncoder.encode("password123"))
						.nickname("테스터")
						.build()
		);
	}

	// Authorization 헤더 값 ("Bearer {토큰}")
	protected String bearer(Member member) {
		return "Bearer " + jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
	}
}

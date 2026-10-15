package kr.ync.triplan.admin.config;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.domain.Role;
import kr.ync.triplan.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminAccountInitializerTest {

	@Autowired
	private MemberRepository memberRepository;

	private Member saveMember(String email) {
		return memberRepository.save(
				Member.builder()
						.email(email)
						.password("encoded")
						.nickname("회원")
						.build()
		);
	}

	@Test
	@DisplayName("설정에 적힌 이메일의 회원은 관리자가 된다 (쉼표로 여러 개, 앞뒤 공백 무시)")
	void promote_listedEmails() {
		saveMember("first@test.com");
		saveMember("second@test.com");

		new AdminAccountInitializer(memberRepository, "first@test.com, second@test.com ").run(null);

		assertThat(memberRepository.findByEmail("first@test.com").orElseThrow().getRole()).isEqualTo(Role.ADMIN);
		assertThat(memberRepository.findByEmail("second@test.com").orElseThrow().getRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("설정에 없는 회원은 그대로 일반 회원이다")
	void notListed_staysUser() {
		saveMember("user@test.com");

		new AdminAccountInitializer(memberRepository, "first@test.com").run(null);

		assertThat(memberRepository.findByEmail("user@test.com").orElseThrow().getRole()).isEqualTo(Role.USER);
	}

	@Test
	@DisplayName("설정이 비어 있거나 가입하지 않은 이메일이면 아무것도 하지 않는다 (오류 없음)")
	void emptyOrUnknown_doesNothing() {
		long before = memberRepository.count();

		new AdminAccountInitializer(memberRepository, "").run(null);
		new AdminAccountInitializer(memberRepository, "nobody@test.com").run(null);

		assertThat(memberRepository.count()).isEqualTo(before);
	}
}
package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder()
						.email(email)
						.password("encodedPassword")
						.nickname("홍길동")
						.build()
		);
	}

	@Test
	@DisplayName("findByEmail - 존재하는 이메일이면 조회됨")
	void findByEmail_success() {
		// given
		savedMember("hong@test.com");

		// when
		Optional<Member> result = memberRepository.findByEmail("hong@test.com");

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getNickname()).isEqualTo("홍길동");
	}

	@Test
	@DisplayName("findByEmail - 존재하지 않으면 빈 값")
	void findByEmail_notFound() {
		// when
		Optional<Member> result = memberRepository.findByEmail("noSuchEmail@test.com");

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("existsByEmail - 중복 이메일 확인")
	void existsByEmail_success() {
		// given
		savedMember("hong@test.com");

		// when & then
		assertThat(memberRepository.existsByEmail("hong@test.com")).isTrue();
		assertThat(memberRepository.existsByEmail("noSuchEmail@test.com")).isFalse();
	}
}

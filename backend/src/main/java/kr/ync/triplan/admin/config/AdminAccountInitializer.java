package kr.ync.triplan.admin.config;

import kr.ync.triplan.member.domain.Role;
import kr.ync.triplan.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class AdminAccountInitializer implements ApplicationRunner {

	private final MemberRepository memberRepository;
	private final String adminEmails;

	public AdminAccountInitializer(MemberRepository memberRepository,
			@Value("${admin.emails:}") String adminEmails) {
		this.memberRepository = memberRepository;
		this.adminEmails = adminEmails;
	}

	@Override
	public void run(ApplicationArguments args) {
		Arrays.stream(adminEmails.split(","))
				.map(String::trim)
				.filter(email -> !email.isEmpty())
				.forEach(this::promote);
	}

	private void promote(String email) {
		memberRepository.findByEmail(email).ifPresentOrElse(
				member -> {
					if (member.getRole() != Role.ADMIN) {
						member.promoteToAdmin();
						memberRepository.save(member);
						log.info("관리자로 지정: {}", email);
					}
				},
				() -> log.warn("관리자로 지정할 회원이 없습니다 (먼저 회원가입 필요): {}", email));
	}
}
package kr.ync.triplan.trip.repository;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.trip.domain.Trip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TripRepositoryTest {

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder().email(email).password("encoded").nickname("닉네임").build());
	}

	private Trip savedTrip(String title, Member owner) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(owner)
						.build()
		);
	}

	@Test
	@DisplayName("findByMemberEmail - 본인 여행만 조회")
	void findByMemberEmail_success() {
		// given
		Member owner = savedMember("owner@test.com");
		Member other = savedMember("other@test.com");
		savedTrip("제주도 여행", owner);
		savedTrip("부산 여행", owner);
		savedTrip("서울 여행", other);

		// when
		List<Trip> result = tripRepository.findByMemberEmail("owner@test.com");

		// then
		assertThat(result).hasSize(2)
				.extracting(Trip::getTitle)
				.containsExactlyInAnyOrder("제주도 여행", "부산 여행");
	}

	@Test
	@DisplayName("findByMemberEmail - 해당 사용자의 여행이 없으면 빈 목록")
	void findByMemberEmail_empty() {
		// when
		List<Trip> result = tripRepository.findByMemberEmail("noSuchUser@test.com");

		// then
		assertThat(result).isEmpty();
	}
}

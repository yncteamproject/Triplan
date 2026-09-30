package kr.ync.triplan.trip.repository;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import org.junit.jupiter.api.BeforeEach;
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
class StopRepositoryTest {

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		Member member = memberRepository.save(
				Member.builder().email("owner@test.com").password("encoded").nickname("주인").build());
		trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(member)
						.build()
		);
	}

	private Stop savedStop(String name, Integer stopOrder) {
		return stopRepository.save(
				Stop.builder()
						.trip(trip)
						.name(name)
						.date(LocalDate.now())
						.stopOrder(stopOrder)
						.build()
		);
	}

	@Test
	@DisplayName("findByTripIdOrderByStopOrderAsc - 순서대로 정렬")
	void findByTripIdOrderByStopOrderAsc_success() {
		// given
		savedStop("세번째", 3);
		savedStop("첫번째", 1);
		savedStop("두번째", 2);

		// when
		List<Stop> result = stopRepository.findByTripIdOrderByStopOrderAsc(trip.getId());

		// then
		assertThat(result)
				.extracting(Stop::getName)
				.containsExactly("첫번째", "두번째", "세번째");
	}
}

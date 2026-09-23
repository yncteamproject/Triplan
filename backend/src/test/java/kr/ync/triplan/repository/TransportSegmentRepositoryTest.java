package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.TransportMode;
import kr.ync.triplan.domain.TransportSegment;
import kr.ync.triplan.domain.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransportSegmentRepositoryTest {

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member member;
	private Trip trip;
	private Stop fromStop;
	private Stop toStop;

	@BeforeEach
	void setUpFixture() {
		member = memberRepository.save(
				Member.builder().email("owner@test.com").password("encoded").nickname("주인").build());
		trip = savedTrip("제주도 여행");
		fromStop = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).build());
		toStop = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).build());
	}

	private Trip savedTrip(String title) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(member)
						.build()
		);
	}

	@Test
	@DisplayName("findByTripId - 해당 여행의 이동 구간만 조회")
	void findByTripId_success() {
		// given
		Trip otherTrip = savedTrip("부산 여행");
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip)
						.fromStop(fromStop)
						.toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.build()
		);
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(otherTrip)
						.fromStop(fromStop)
						.toStop(toStop)
						.mode(TransportMode.BUS)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.build()
		);

		// when
		List<TransportSegment> result = transportSegmentRepository.findByTripId(trip.getId());

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getMode()).isEqualTo(TransportMode.CAR);
	}
}

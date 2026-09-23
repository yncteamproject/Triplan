package kr.ync.triplan.repository;

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

	private Trip trip;
	private Stop fromStop;
	private Stop toStop;

	@BeforeEach
	void setUpFixture() {
		trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId("user1")
						.build()
		);
		fromStop = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).build());
		toStop = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).build());
	}

	@Test
	@DisplayName("findByTripId - 해당 여행의 이동 구간만 조회")
	void findByTripId_success() {
		// given
		Trip otherTrip = tripRepository.save(
				Trip.builder()
						.title("부산 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(2))
						.userId("user2")
						.build()
		);
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

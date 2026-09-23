package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Lodging;
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
class LodgingRepositoryTest {

	@Autowired
	private LodgingRepository lodgingRepository;

	@Autowired
	private TripRepository tripRepository;

	private Trip trip;

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
	}

	@Test
	@DisplayName("findByTripId - 해당 여행의 숙소만 조회")
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
		lodgingRepository.save(
				Lodging.builder()
						.trip(trip)
						.name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(otherTrip)
						.name("부산 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.build()
		);

		// when
		List<Lodging> result = lodgingRepository.findByTripId(trip.getId());

		// then
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getName()).isEqualTo("제주 호텔");
	}
}

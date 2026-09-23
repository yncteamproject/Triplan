package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Trip;
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

	private Trip savedTrip(String title, String userId) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId(userId)
						.build()
		);
	}

	@Test
	@DisplayName("findByUserId - 본인 여행만 조회")
	void findByUserId_success() {
		// given
		savedTrip("제주도 여행", "user1");
		savedTrip("부산 여행", "user1");
		savedTrip("서울 여행", "user2");

		// when
		List<Trip> result = tripRepository.findByUserId("user1");

		// then
		assertThat(result).hasSize(2)
				.extracting(Trip::getTitle)
				.containsExactlyInAnyOrder("제주도 여행", "부산 여행");
	}

	@Test
	@DisplayName("findByUserId - 해당 사용자의 여행이 없으면 빈 목록")
	void findByUserId_empty() {
		// when
		List<Trip> result = tripRepository.findByUserId("noSuchUser");

		// then
		assertThat(result).isEmpty();
	}
}

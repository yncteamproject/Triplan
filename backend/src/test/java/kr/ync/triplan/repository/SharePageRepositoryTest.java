package kr.ync.triplan.repository;

import kr.ync.triplan.domain.SharePage;
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
class SharePageRepositoryTest {

	@Autowired
	private SharePageRepository sharePageRepository;

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

	private SharePage savedSharePage(String title, LocalDateTime writeDate) {
		return sharePageRepository.save(
				SharePage.builder()
						.title(title)
						.description("설명")
						.trip(trip)
						.writerId("user1")
						.writeDate(writeDate)
						.viewCount(0)
						.build()
		);
	}

	@Test
	@DisplayName("findAllByOrderByWriteDateDesc - 작성일 최신순 정렬")
	void findAllByOrderByWriteDateDesc_success() {
		// given
		LocalDateTime now = LocalDateTime.now();
		savedSharePage("먼저 쓴 글", now.minusDays(2));
		savedSharePage("나중에 쓴 글", now);
		savedSharePage("중간에 쓴 글", now.minusDays(1));

		// when
		List<SharePage> result = sharePageRepository.findAllByOrderByWriteDateDesc();

		// then
		assertThat(result)
				.extracting(SharePage::getTitle)
				.containsExactly("나중에 쓴 글", "중간에 쓴 글", "먼저 쓴 글");
	}

	@Test
	@DisplayName("findByTripId - 특정 여행에 속한 게시글만 조회")
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
		savedSharePage("제주도 글1", LocalDateTime.now());
		savedSharePage("제주도 글2", LocalDateTime.now());
		sharePageRepository.save(
				SharePage.builder()
						.title("부산 글")
						.description("설명")
						.trip(otherTrip)
						.writerId("user2")
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);

		// when
		List<SharePage> result = sharePageRepository.findByTripId(trip.getId());

		// then
		assertThat(result).hasSize(2)
				.extracting(SharePage::getTitle)
				.containsExactlyInAnyOrder("제주도 글1", "제주도 글2");
	}
}

package kr.ync.triplan.share.repository;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	@Autowired
	private MemberRepository memberRepository;

	private Member member;
	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		member = memberRepository.save(
				Member.builder().email("writer@test.com").password("encoded").nickname("홍길동").build());
		trip = savedTrip("제주도 여행");
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

	private SharePage savedSharePage(String title, Trip trip, LocalDateTime writeDate) {
		return sharePageRepository.save(
				SharePage.builder()
						.title(title)
						.description("설명")
						.trip(trip)
						.writer(member)
						.writeDate(writeDate)
						.viewCount(0)
						.build()
		);
	}

	@Test
	@DisplayName("findAll(Pageable) - 작성일 최신순으로 페이지 크기만큼 조회")
	void findAll_paging() {
		// given
		LocalDateTime now = LocalDateTime.now();
		savedSharePage("먼저 쓴 글", trip, now.minusDays(2));
		savedSharePage("나중에 쓴 글", trip, now);
		savedSharePage("중간에 쓴 글", trip, now.minusDays(1));

		// when
		Page<SharePage> result = sharePageRepository.findAll(
				PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "writeDate")));

		// then
		assertThat(result.getContent())
				.extracting(SharePage::getTitle)
				.containsExactly("나중에 쓴 글", "중간에 쓴 글");
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.hasNext()).isTrue();
	}

	@Test
	@DisplayName("findByTripId - 특정 여행에 속한 게시글만 조회")
	void findByTripId_success() {
		// given
		Trip otherTrip = savedTrip("부산 여행");
		savedSharePage("제주도 글1", trip, LocalDateTime.now());
		savedSharePage("제주도 글2", trip, LocalDateTime.now());
		savedSharePage("부산 글", otherTrip, LocalDateTime.now());

		// when
		List<SharePage> result = sharePageRepository.findByTripId(trip.getId());

		// then
		assertThat(result).hasSize(2)
				.extracting(SharePage::getTitle)
				.containsExactlyInAnyOrder("제주도 글1", "제주도 글2");
	}
}

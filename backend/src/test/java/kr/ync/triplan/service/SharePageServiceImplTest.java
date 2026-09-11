package kr.ync.triplan.service;

import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.SharePageCreateRequest;
import kr.ync.triplan.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.dto.response.SharePageListResponse;
import kr.ync.triplan.dto.response.SharePageResponse;
import kr.ync.triplan.exception.SharePageNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.SharePageRepository;
import kr.ync.triplan.repository.TripRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class SharePageServiceImplTest {

	@Autowired
	private SharePageService sharePageService;

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private TripRepository tripRepository;

	private static final long NON_EXISTING_ID = 99999L;

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

	private SharePage savedSharePage(String title, String description, String writerId) {
		return sharePageRepository.save(
				SharePage.builder()
						.title(title)
						.description(description)
						.trip(trip)
						.writerId(writerId)
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
	}

	// CRUD
	@Test
	@DisplayName("create")
	void create_success() {
		// given 준비
		SharePageCreateRequest request = new SharePageCreateRequest("제목1", "내용1", trip.getId(), "홍길동");
		// when 실행
		SharePageResponse response = sharePageService.create(request);
		// then 검증
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(
						SharePageResponse::title,
						SharePageResponse::description,
						SharePageResponse::tripId,
						SharePageResponse::writerId
				)
				.containsExactly("제목1", "내용1", trip.getId(), "홍길동");
	}

	@Test
	@DisplayName("create - 존재하지 않는 여행이면 예외")
	void create_tripNotFound() {
		// given
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "내용", NON_EXISTING_ID, "홍길동");
		// when & then
		assertThatThrownBy(() -> sharePageService.create(request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("getDetail - 조회수 증가")
	void getDetail_success() {
		// given
		SharePage saved = savedSharePage("제목", "내용", "작성자");
		// when
		SharePageResponse response = sharePageService.getDetail(saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response.viewCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> sharePageService.getDetail(NON_EXISTING_ID))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("list - 최신순 목록조회")
	void list_success() {
		// given
		savedSharePage("t1", "c1", "a1");
		savedSharePage("t2", "c2", "a2");
		savedSharePage("t3", "c3", "a3");
		// when
		List<SharePageListResponse> list = sharePageService.getList();
		// then
		assertThat(list).hasSize(3)
				.extracting(SharePageListResponse::title)
				.containsExactlyInAnyOrder("t1", "t2", "t3");
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		SharePage saved = savedSharePage("제목", "내용", "작성자");
		// when
		SharePageResponse response = sharePageService.update(
				saved.getId(),
				new SharePageUpdateRequest("제목2", "내용2")
		);
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response)
				.extracting(
						SharePageResponse::title,
						SharePageResponse::description
				)
				.containsExactly("제목2", "내용2");
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		assertThatThrownBy(() -> sharePageService.update(NON_EXISTING_ID, new SharePageUpdateRequest("제목", "내용")))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		SharePage saved = savedSharePage("제목", "내용", "작성자");
		// when
		sharePageService.delete(saved.getId());
		// then
		assertThatThrownBy(() -> sharePageService.getDetail(saved.getId()))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> sharePageService.delete(NON_EXISTING_ID))
				.isInstanceOf(SharePageNotFoundException.class);
	}
}

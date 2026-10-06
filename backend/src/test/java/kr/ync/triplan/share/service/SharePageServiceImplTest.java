package kr.ync.triplan.share.service;

import jakarta.persistence.EntityManager;
import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.dto.response.SharedTripResponse;
import kr.ync.triplan.share.exception.SharePageNotFoundException;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportMode;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
import kr.ync.triplan.trip.repository.TripRepository;
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

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private LodgingRepository lodgingRepository;

	private static final long NON_EXISTING_ID = 99999L;

	private Member member;
	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		member = savedMember("writer@test.com");
		trip = savedTrip(member);
	}

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder().email(email).password("encoded").nickname("홍길동").build());
	}

	private Trip savedTrip(Member owner) {
		return tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(owner)
						.build()
		);
	}

	private SharePage savedSharePage(String title, String description) {
		return sharePageRepository.save(
				SharePage.builder()
						.title(title)
						.description(description)
						.trip(trip)
						.writer(member)
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
		SharePageCreateRequest request = new SharePageCreateRequest("제목1", "내용1", trip.getId());
		// when 실행
		SharePageResponse response = sharePageService.create(member.getEmail(), request);
		// then 검증
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(
						SharePageResponse::title,
						SharePageResponse::description,
						SharePageResponse::tripId,
						SharePageResponse::writerId,
						SharePageResponse::writerNickname
				)
				.containsExactly("제목1", "내용1", trip.getId(), member.getId(), "홍길동");
	}

	@Test
	@DisplayName("create - 존재하지 않는 여행이면 예외")
	void create_tripNotFound() {
		// given
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "내용", NON_EXISTING_ID);
		// when & then
		assertThatThrownBy(() -> sharePageService.create(member.getEmail(), request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("create - 남의 여행을 공유하면 예외")
	void create_othersTripForbidden() {
		// given
		Trip othersTrip = savedTrip(savedMember("other@test.com"));
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "내용", othersTrip.getId());
		// when & then
		assertThatThrownBy(() -> sharePageService.create(member.getEmail(), request))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("getDetail - 조회수 증가")
	void getDetail_success() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
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
		savedSharePage("t1", "c1");
		savedSharePage("t2", "c2");
		savedSharePage("t3", "c3");
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
		SharePage saved = savedSharePage("제목", "내용");
		// when
		SharePageResponse response = sharePageService.update(
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목2", "내용2"));
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response)
				.extracting(SharePageResponse::title, SharePageResponse::description)
				.containsExactly("제목2", "내용2");
	}

	@Test
	@DisplayName("update - 설명 2000자(최대 길이)로 수정해도 저장됨")
	void update_maxLengthDescription() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		String maxDescription = "가".repeat(2000);
		// when
		sharePageService.update(
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목", maxDescription));
		entityManager.flush(); // 수정 SQL을 실제로 DB에 보내서 컬럼 길이 초과 여부 확인
		// then
		assertThat(sharePageRepository.findById(saved.getId()).orElseThrow().getDescription())
				.hasSize(2000);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		assertThatThrownBy(() -> sharePageService.update(
				member.getEmail(), NON_EXISTING_ID, new SharePageUpdateRequest("제목", "내용")))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("update - 작성자가 아니면 예외")
	void update_forbidden() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		// when & then
		assertThatThrownBy(() -> sharePageService.update(
				"other@test.com", saved.getId(), new SharePageUpdateRequest("제목2", "내용2")))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		// when
		sharePageService.delete(member.getEmail(), saved.getId());
		// then
		assertThatThrownBy(() -> sharePageService.getDetail(saved.getId()))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> sharePageService.delete(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("delete - 작성자가 아니면 예외")
	void delete_forbidden() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		// when & then
		assertThatThrownBy(() -> sharePageService.delete("other@test.com", saved.getId()))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("delete - 댓글이 달린 게시글도 삭제되고, 댓글도 함께 삭제됨")
	void delete_withComments() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		Comment comment = commentRepository.save(
				Comment.builder()
						.content("좋아요").sharePage(saved)
						.writer(savedMember("other@test.com"))
						.createdAt(LocalDateTime.now())
						.build()
		);
		entityManager.flush();

		// when
		sharePageService.delete(member.getEmail(), saved.getId());
		entityManager.flush(); // 삭제 SQL을 실제로 DB에 보내서 외래키 위반 여부 확인

		// then
		assertThat(sharePageRepository.findById(saved.getId())).isEmpty();
		assertThat(commentRepository.findById(comment.getId())).isEmpty();
	}

	@Test
	@DisplayName("getSharedTrip - 공유된 여행의 방문지·이동 구간·숙소·경비 조회")
	void getSharedTrip_success() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		Stop second = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).stopOrder(2).build());
		Stop first = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).stopOrder(1).build());
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip).fromStop(first).toStop(second)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.cost(20000).reservationNo("RES-SECRET")
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(trip).name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.cost(100000).reservationNo("RES-SECRET")
						.build()
		);

		// when
		SharedTripResponse response = sharePageService.getSharedTrip(saved.getId());

		// then
		assertThat(response.tripId()).isEqualTo(trip.getId());
		assertThat(response.stops())
				.extracting(SharedTripResponse.StopItem::name)
				.containsExactly("공항", "숙소");
		assertThat(response.transportSegments()).hasSize(1);
		assertThat(response.transportSegments().getFirst())
				.extracting(SharedTripResponse.TransportSegmentItem::fromStopId, SharedTripResponse.TransportSegmentItem::toStopId)
				.containsExactly(first.getId(), second.getId());
		assertThat(response.lodgings()).hasSize(1);
		assertThat(response)
				.extracting(SharedTripResponse::transportCost, SharedTripResponse::lodgingCost, SharedTripResponse::totalCost)
				.containsExactly(20000, 100000, 120000);
	}

	@Test
	@DisplayName("getSharedTrip - 존재하지 않는 게시글이면 예외")
	void getSharedTrip_notFound() {
		assertThatThrownBy(() -> sharePageService.getSharedTrip(NON_EXISTING_ID))
				.isInstanceOf(SharePageNotFoundException.class);
	}
}

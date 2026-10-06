package kr.ync.triplan.share.service;

import jakarta.persistence.EntityManager;
import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.request.TripCopyRequest;
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
import kr.ync.triplan.trip.dto.response.TripResponse;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import kr.ync.triplan.trip.service.TripService;
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

	@Autowired
	private TripService tripService;

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
		SharePageCreateRequest request = new SharePageCreateRequest("제목1", "내용1", trip.getId(), null);
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
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "내용", NON_EXISTING_ID, null);
		// when & then
		assertThatThrownBy(() -> sharePageService.create(member.getEmail(), request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("create - 남의 여행을 공유하면 예외")
	void create_othersTripForbidden() {
		// given
		Trip othersTrip = savedTrip(savedMember("other@test.com"));
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "내용", othersTrip.getId(), null);
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
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목2", "내용2", null));
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
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목", maxDescription, null));
		entityManager.flush(); // 수정 SQL을 실제로 DB에 보내서 컬럼 길이 초과 여부 확인
		// then
		assertThat(sharePageRepository.findById(saved.getId()).orElseThrow().getDescription())
				.hasSize(2000);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		assertThatThrownBy(() -> sharePageService.update(
				member.getEmail(), NON_EXISTING_ID, new SharePageUpdateRequest("제목", "내용", null)))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("update - 작성자가 아니면 예외")
	void update_forbidden() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		// when & then
		assertThatThrownBy(() -> sharePageService.update(
				"other@test.com", saved.getId(), new SharePageUpdateRequest("제목2", "내용2", null)))
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

	// 복사 허용 설정 (S5)
	@Test
	@DisplayName("create - allowCopy를 안 보내면 복사 허용(true), 복사 수는 0")
	void create_allowCopyDefault() {
		SharePageResponse response = sharePageService.create(
				member.getEmail(), new SharePageCreateRequest("제목", "내용", trip.getId(), null));

		assertThat(response.allowCopy()).isTrue();
		assertThat(response.copyCount()).isZero();
	}

	@Test
	@DisplayName("create - allowCopy를 false로 보내면 복사 불가로 저장")
	void create_allowCopyFalse() {
		SharePageResponse response = sharePageService.create(
				member.getEmail(), new SharePageCreateRequest("제목", "내용", trip.getId(), false));

		assertThat(response.allowCopy()).isFalse();
	}

	@Test
	@DisplayName("update - allowCopy를 바꿀 수 있고, 안 보내면 기존 값 유지")
	void update_allowCopy() {
		// given
		SharePage saved = savedSharePage("제목", "내용");
		// when & then
		SharePageResponse changed = sharePageService.update(
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목", "내용", false));
		assertThat(changed.allowCopy()).isFalse();

		SharePageResponse kept = sharePageService.update(
				member.getEmail(), saved.getId(), new SharePageUpdateRequest("제목2", "내용2", null));
		assertThat(kept.allowCopy()).isFalse();
	}

	// 여행 복사 (S5)
	// 원본 여행에 방문지 2개(공항 → 숙소), 이동 구간 1개, 숙소 1개를 넣는다
	private void saveTripDetails() {
		Stop first = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(trip.getStartDate()).stopOrder(1).build());
		Stop second = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(trip.getStartDate().plusDays(1)).stopOrder(2).build());
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip).fromStop(first).toStop(second)
						.mode(TransportMode.CAR)
						.departTime(trip.getStartDate().atTime(10, 0))
						.arriveTime(trip.getStartDate().atTime(11, 0))
						.cost(20000).reservationNo("RES-SECRET")
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(trip).name("제주 호텔")
						.checkIn(trip.getStartDate().atTime(15, 0))
						.checkOut(trip.getStartDate().plusDays(1).atTime(11, 0))
						.cost(100000).reservationNo("RES-SECRET")
						.build()
		);
	}

	@Test
	@DisplayName("copyTrip - 여행·방문지·이동 구간·숙소가 내 여행으로 복사되고 복사 수가 오름")
	void copyTrip_success() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saveTripDetails();
		Member copier = savedMember("copier@test.com");

		// when
		TripResponse response = sharePageService.copyTrip(copier.getEmail(), saved.getId(), null);

		// then: 원본과 다른 여행이 내 여행 목록에 생김 (날짜는 원본 그대로)
		assertThat(response.id()).isNotEqualTo(trip.getId());
		assertThat(response)
				.extracting(TripResponse::title, TripResponse::startDate, TripResponse::endDate)
				.containsExactly(trip.getTitle(), trip.getStartDate(), trip.getEndDate());
		assertThat(tripRepository.findByMemberEmail(copier.getEmail()))
				.extracting(Trip::getId)
				.containsExactly(response.id());

		// 이동 구간의 출발 · 도착지는 원본이 아니라 복사된 방문지
		List<Stop> copiedStops = stopRepository.findByTripIdOrderByStopOrderAsc(response.id());
		assertThat(copiedStops).extracting(Stop::getName).containsExactly("공항", "숙소");
		List<TransportSegment> copiedSegments = transportSegmentRepository.findByTripId(response.id());
		assertThat(copiedSegments).hasSize(1);
		assertThat(copiedSegments.getFirst().getFromStop().getId()).isEqualTo(copiedStops.get(0).getId());
		assertThat(copiedSegments.getFirst().getToStop().getId()).isEqualTo(copiedStops.get(1).getId());

		// 비용은 복사, 예약번호는 비움 (B12)
		assertThat(copiedSegments.getFirst().getCost()).isEqualTo(20000);
		assertThat(copiedSegments.getFirst().getReservationNo()).isNull();
		List<Lodging> copiedLodgings = lodgingRepository.findByTripId(response.id());
		assertThat(copiedLodgings).hasSize(1);
		assertThat(copiedLodgings.getFirst().getCost()).isEqualTo(100000);
		assertThat(copiedLodgings.getFirst().getReservationNo()).isNull();

		assertThat(sharePageRepository.findById(saved.getId()).orElseThrow().getCopyCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("copyTrip - 시작일을 지정하면 모든 날짜가 같은 간격으로 이동")
	void copyTrip_withStartDate() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saveTripDetails();
		Member copier = savedMember("copier@test.com");
		LocalDate newStartDate = trip.getStartDate().plusDays(10);

		// when
		TripResponse response = sharePageService.copyTrip(
				copier.getEmail(), saved.getId(), new TripCopyRequest(newStartDate));

		// then
		assertThat(response.startDate()).isEqualTo(newStartDate);
		assertThat(response.endDate()).isEqualTo(trip.getEndDate().plusDays(10));
		assertThat(stopRepository.findByTripIdOrderByStopOrderAsc(response.id()))
				.extracting(Stop::getDate)
				.containsExactly(newStartDate, newStartDate.plusDays(1));
		TransportSegment copiedSegment = transportSegmentRepository.findByTripId(response.id()).getFirst();
		assertThat(copiedSegment.getDepartTime()).isEqualTo(newStartDate.atTime(10, 0));
		assertThat(copiedSegment.getArriveTime()).isEqualTo(newStartDate.atTime(11, 0));
		Lodging copiedLodging = lodgingRepository.findByTripId(response.id()).getFirst();
		assertThat(copiedLodging.getCheckIn()).isEqualTo(newStartDate.atTime(15, 0));
		assertThat(copiedLodging.getCheckOut()).isEqualTo(newStartDate.plusDays(1).atTime(11, 0));
	}

	@Test
	@DisplayName("copyTrip - 원본 여행을 삭제해도 복사한 여행은 남아 있음")
	void copyTrip_survivesOriginalDelete() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saveTripDetails();
		Member copier = savedMember("copier@test.com");
		TripResponse response = sharePageService.copyTrip(copier.getEmail(), saved.getId(), null);

		// when
		tripService.delete(member.getEmail(), trip.getId());
		entityManager.flush(); // 삭제 SQL을 실제로 DB에 보내서 외래키 위반 여부 확인

		// then
		assertThat(tripRepository.findById(trip.getId())).isEmpty();
		assertThat(tripRepository.findById(response.id())).isPresent();
		assertThat(stopRepository.findByTripIdOrderByStopOrderAsc(response.id())).hasSize(2);
		assertThat(transportSegmentRepository.findByTripId(response.id())).hasSize(1);
		assertThat(lodgingRepository.findByTripId(response.id())).hasSize(1);
	}

	@Test
	@DisplayName("copyTrip - 복사를 허용하지 않은 게시글을 다른 사람이 복사하면 예외")
	void copyTrip_notAllowed() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saved.setAllowCopy(false);
		Member copier = savedMember("copier@test.com");

		// when & then
		assertThatThrownBy(() -> sharePageService.copyTrip(copier.getEmail(), saved.getId(), null))
				.isInstanceOf(ForbiddenException.class);
		assertThat(tripRepository.findByMemberEmail(copier.getEmail())).isEmpty();
		assertThat(saved.getCopyCount()).isZero();
	}

	@Test
	@DisplayName("copyTrip - 복사를 허용하지 않아도 작성자 본인은 복사 가능")
	void copyTrip_notAllowed_writerCanCopy() {
		// given
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saved.setAllowCopy(false);

		// when
		TripResponse response = sharePageService.copyTrip(member.getEmail(), saved.getId(), null);

		// then
		assertThat(response.id()).isNotEqualTo(trip.getId());
	}

	@Test
	@DisplayName("copyTrip - 존재하지 않는 게시글이면 예외")
	void copyTrip_notFound() {
		assertThatThrownBy(() -> sharePageService.copyTrip(member.getEmail(), NON_EXISTING_ID, null))
				.isInstanceOf(SharePageNotFoundException.class);
	}
}

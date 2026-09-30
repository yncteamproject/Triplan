package kr.ync.triplan.trip.service;

import jakarta.persistence.EntityManager;
import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportMode;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.TripCreateRequest;
import kr.ync.triplan.trip.dto.request.TripUpdateRequest;
import kr.ync.triplan.trip.dto.response.TripEstimateResponse;
import kr.ync.triplan.trip.dto.response.TripResponse;
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
class TripServiceImplTest {

	@Autowired
	private TripService tripService;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private LodgingRepository lodgingRepository;

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private EntityManager entityManager;

	private static final long NON_EXISTING_ID = 99999L;

	private Member member;

	@BeforeEach
	void setUpFixture() {
		member = savedMember("owner@test.com");
	}

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder().email(email).password("encoded").nickname("주인").build());
	}

	private Trip savedTrip(String title, Member owner) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(owner)
						.build()
		);
	}

	@Test
	@DisplayName("create")
	void create_success() {
		// given
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().plusDays(3));
		// when
		TripResponse response = tripService.create(member.getEmail(), request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response.title()).isEqualTo("제주도 여행");
		assertThat(tripRepository.findById(response.id()).orElseThrow().getMember().getId())
				.isEqualTo(member.getId());
	}

	@Test
	@DisplayName("getDetail")
	void getDetail_success() {
		// given
		Trip saved = savedTrip("제주도 여행", member);
		// when
		TripResponse response = tripService.getDetail(member.getEmail(), saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response.title()).isEqualTo("제주도 여행");
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> tripService.getDetail(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("getDetail - 남의 여행이면 예외")
	void getDetail_forbidden() {
		// given
		Trip othersTrip = savedTrip("남의 여행", savedMember("other@test.com"));
		// when & then
		assertThatThrownBy(() -> tripService.getDetail(member.getEmail(), othersTrip.getId()))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("list - 본인 여행만 조회")
	void list_success() {
		// given
		savedTrip("제주도 여행", member);
		savedTrip("부산 여행", member);
		savedTrip("남의 여행", savedMember("other@test.com"));
		// when
		List<TripResponse> list = tripService.getList(member.getEmail());
		// then
		assertThat(list).hasSize(2)
				.extracting(TripResponse::title)
				.containsExactlyInAnyOrder("제주도 여행", "부산 여행");
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		Trip saved = savedTrip("원래 제목", member);
		LocalDate newStart = LocalDate.now().plusDays(10);
		LocalDate newEnd = LocalDate.now().plusDays(15);
		// when
		TripResponse response = tripService.update(
				member.getEmail(), saved.getId(), new TripUpdateRequest("변경된 제목", newStart, newEnd));
		// then
		assertThat(response)
				.extracting(TripResponse::title, TripResponse::startDate, TripResponse::endDate)
				.containsExactly("변경된 제목", newStart, newEnd);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		TripUpdateRequest request = new TripUpdateRequest(
				"제목", LocalDate.now(), LocalDate.now().plusDays(1));
		assertThatThrownBy(() -> tripService.update(member.getEmail(), NON_EXISTING_ID, request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("update - 남의 여행이면 예외")
	void update_forbidden() {
		// given
		Trip othersTrip = savedTrip("남의 여행", savedMember("other@test.com"));
		TripUpdateRequest request = new TripUpdateRequest(
				"제목", LocalDate.now(), LocalDate.now().plusDays(1));
		// when & then
		assertThatThrownBy(() -> tripService.update(member.getEmail(), othersTrip.getId(), request))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		Trip saved = savedTrip("제목", member);
		// when
		tripService.delete(member.getEmail(), saved.getId());
		// then
		assertThatThrownBy(() -> tripService.getDetail(member.getEmail(), saved.getId()))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> tripService.delete(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("delete - 방문지·이동 구간·숙소·공유 게시글·댓글이 있어도 함께 삭제됨")
	void delete_withChildren() {
		// given
		Trip saved = savedTrip("제주도 여행", member);
		Stop fromStop = stopRepository.save(
				Stop.builder().trip(saved).name("공항").date(LocalDate.now()).build());
		Stop toStop = stopRepository.save(
				Stop.builder().trip(saved).name("숙소").date(LocalDate.now()).build());
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(saved).fromStop(fromStop).toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(saved).name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.build()
		);
		SharePage sharePage = sharePageRepository.save(
				SharePage.builder()
						.title("제주도 후기").trip(saved).writer(member)
						.writeDate(LocalDateTime.now())
						.build()
		);
		commentRepository.save(
				Comment.builder()
						.content("좋아요").sharePage(sharePage)
						.writer(savedMember("other@test.com"))
						.createdAt(LocalDateTime.now())
						.build()
		);
		entityManager.flush();

		// when
		tripService.delete(member.getEmail(), saved.getId());
		entityManager.flush(); // 삭제 SQL을 실제로 DB에 보내서 외래키 위반 여부 확인

		// then
		assertThat(tripRepository.findById(saved.getId())).isEmpty();
		assertThat(stopRepository.findByTripIdOrderByStopOrderAsc(saved.getId())).isEmpty();
		assertThat(transportSegmentRepository.findByTripId(saved.getId())).isEmpty();
		assertThat(lodgingRepository.findByTripId(saved.getId())).isEmpty();
		assertThat(sharePageRepository.findByTripId(saved.getId())).isEmpty();
		assertThat(commentRepository.findBySharePageIdOrderByCreatedAtAsc(sharePage.getId())).isEmpty();
	}

	@Test
	@DisplayName("getEstimate - 교통비/숙박비 합산")
	void getEstimate_success() {
		// given
		Trip saved = savedTrip("제주도 여행", member);
		Stop fromStop = stopRepository.save(
				Stop.builder().trip(saved).name("공항").date(LocalDate.now()).build());
		Stop toStop = stopRepository.save(
				Stop.builder().trip(saved).name("숙소").date(LocalDate.now()).build());
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(saved).fromStop(fromStop).toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.cost(20000)
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(saved).name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.cost(100000)
						.build()
		);

		// when
		TripEstimateResponse response = tripService.getEstimate(member.getEmail(), saved.getId());

		// then
		assertThat(response)
				.extracting(TripEstimateResponse::transportCost, TripEstimateResponse::lodgingCost, TripEstimateResponse::totalCost)
				.containsExactly(20000, 100000, 120000);
	}

	@Test
	@DisplayName("getEstimate - 데이터 없으면 0원")
	void getEstimate_noData() {
		// given
		Trip saved = savedTrip("제주도 여행", member);
		// when
		TripEstimateResponse response = tripService.getEstimate(member.getEmail(), saved.getId());
		// then
		assertThat(response.totalCost()).isZero();
	}

	@Test
	@DisplayName("getEstimate - 존재하지 않으면 예외")
	void getEstimate_notFound() {
		assertThatThrownBy(() -> tripService.getEstimate(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(TripNotFoundException.class);
	}
}

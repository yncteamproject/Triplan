package kr.ync.triplan.trip.service;

import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.StopCreateRequest;
import kr.ync.triplan.trip.dto.request.StopUpdateRequest;
import kr.ync.triplan.trip.dto.response.StopResponse;
import kr.ync.triplan.trip.exception.StopNotFoundException;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class StopServiceImplTest {

	@Autowired
	private StopService stopService;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private MemberRepository memberRepository;

	private static final long NON_EXISTING_ID = 99999L;

	private Member member;
	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		member = savedMember("owner@test.com");
		trip = savedTrip(member);
	}

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder().email(email).password("encoded").nickname("주인").build());
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

	private Stop savedStop(String name, Integer stopOrder) {
		return stopRepository.save(
				Stop.builder()
						.trip(trip)
						.name(name)
						.date(LocalDate.now())
						.stopOrder(stopOrder)
						.build()
		);
	}

	@Test
	@DisplayName("create")
	void create_success() {
		// given
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);
		// when
		StopResponse response = stopService.create(member.getEmail(), trip.getId(), request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(StopResponse::name, StopResponse::tripId, StopResponse::stopOrder)
				.containsExactly("성산일출봉", trip.getId(), 1);
	}

	@Test
	@DisplayName("create - 존재하지 않는 여행이면 예외")
	void create_tripNotFound() {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);

		assertThatThrownBy(() -> stopService.create(member.getEmail(), NON_EXISTING_ID, request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("create - 남의 여행이면 예외")
	void create_forbidden() {
		// given
		Trip othersTrip = savedTrip(savedMember("other@test.com"));
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);
		// when & then
		assertThatThrownBy(() -> stopService.create(member.getEmail(), othersTrip.getId(), request))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("getDetail")
	void getDetail_success() {
		// given
		Stop saved = savedStop("성산일출봉", 1);
		// when
		StopResponse response = stopService.getDetail(member.getEmail(), saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
		assertThat(response.name()).isEqualTo("성산일출봉");
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> stopService.getDetail(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(StopNotFoundException.class);
	}

	@Test
	@DisplayName("getDetail - 남의 방문지면 예외")
	void getDetail_forbidden() {
		// given
		Stop saved = savedStop("성산일출봉", 1);
		// when & then
		assertThatThrownBy(() -> stopService.getDetail("other@test.com", saved.getId()))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("list - 순서대로 조회")
	void list_success() {
		// given
		savedStop("두번째", 2);
		savedStop("첫번째", 1);
		// when
		List<StopResponse> list = stopService.getList(member.getEmail(), trip.getId());
		// then
		assertThat(list)
				.extracting(StopResponse::name)
				.containsExactly("첫번째", "두번째");
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		Stop saved = savedStop("원래 이름", 1);
		// when
		StopResponse response = stopService.update(
				member.getEmail(), saved.getId(),
				new StopUpdateRequest("변경된 이름", LocalDate.now(), LocalTime.of(10, 0), "메모", null, 2));
		// then
		assertThat(response)
				.extracting(StopResponse::name, StopResponse::stopOrder)
				.containsExactly("변경된 이름", 2);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		StopUpdateRequest request = new StopUpdateRequest(
				"이름", LocalDate.now(), null, null, null, 1);
		assertThatThrownBy(() -> stopService.update(member.getEmail(), NON_EXISTING_ID, request))
				.isInstanceOf(StopNotFoundException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		Stop saved = savedStop("성산일출봉", 1);
		// when
		stopService.delete(member.getEmail(), saved.getId());
		// then
		assertThatThrownBy(() -> stopService.getDetail(member.getEmail(), saved.getId()))
				.isInstanceOf(StopNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> stopService.delete(member.getEmail(), NON_EXISTING_ID))
				.isInstanceOf(StopNotFoundException.class);
	}
}

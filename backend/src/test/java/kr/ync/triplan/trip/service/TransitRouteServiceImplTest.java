package kr.ync.triplan.trip.service;

import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.trip.client.OdsayClient;
import kr.ync.triplan.support.OdsayFixture;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse;
import kr.ync.triplan.trip.exception.InvalidTransitRouteRequestException;
import kr.ync.triplan.trip.exception.StopNotFoundException;
import kr.ync.triplan.trip.exception.TransitApiException;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 오디세이는 실제로 호출하지 않고 가짜(Mock)로 바꿔서 테스트한다 (무료 호출 하루 30건, 외부 서버 상태와 무관하게)
@SpringBootTest
@Transactional
class TransitRouteServiceImplTest {

	@Autowired
	private TransitRouteService transitRouteService;

	@Autowired
	private TransitRouteCache transitRouteCache;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private MemberRepository memberRepository;

	@MockitoBean
	private OdsayClient odsayClient;

	private static final long NON_EXISTING_ID = 99999L;

	private Member member;
	private Trip trip;
	private Stop seoulStation;
	private Stop gangnamStation;

	@BeforeEach
	void setUpFixture() {
		transitRouteCache.clear(); // 캐시는 메모리에 남아 있어서 테스트마다 비운다
		member = savedMember("owner@test.com");
		trip = savedTrip(member);
		seoulStation = savedStop(trip, "서울역", 37.5547, 126.9707);
		gangnamStation = savedStop(trip, "강남역", 37.4979, 127.0276);
	}

	private Member savedMember(String email) {
		return memberRepository.save(
				Member.builder().email(email).password("encoded").nickname("주인").build());
	}

	private Trip savedTrip(Member owner) {
		return tripRepository.save(
				Trip.builder()
						.title("서울 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(1))
						.member(owner)
						.build()
		);
	}

	private Stop savedStop(Trip trip, String name, Double latitude, Double longitude) {
		return stopRepository.save(
				Stop.builder()
						.trip(trip).name(name).date(LocalDate.now())
						.latitude(latitude).longitude(longitude)
						.build()
		);
	}

	@Test
	@DisplayName("getRoutes - 두 방문지의 경도 · 위도 순서로 오디세이를 호출하고 경로 후보를 반환")
	void getRoutes_success() {
		// given
		given(odsayClient.searchPubTransPath(126.9707, 37.5547, 127.0276, 37.4979))
				.willReturn(OdsayFixture.SUCCESS_JSON);

		// when
		TransitRouteResponse response = transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());

		// then
		assertThat(response.routes())
				.extracting(TransitRouteResponse.Route::totalTime)
				.containsExactly(33, 36, 39);
	}

	@Test
	@DisplayName("getRoutes - 같은 두 방문지를 다시 조회하면 오디세이를 다시 호출하지 않음 (캐시)")
	void getRoutes_cached() {
		// given
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(OdsayFixture.SUCCESS_JSON);

		// when
		TransitRouteResponse first = transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());
		TransitRouteResponse second = transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());

		// then
		assertThat(second).isEqualTo(first);
		verify(odsayClient, times(1)).searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble());
	}

	@Test
	@DisplayName("getRoutes - 방향이 반대면 다른 경로라서 다시 호출")
	void getRoutes_reverseDirection() {
		// given
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(OdsayFixture.SUCCESS_JSON);

		// when
		transitRouteService.getRoutes(member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());
		transitRouteService.getRoutes(member.getEmail(), trip.getId(), gangnamStation.getId(), seoulStation.getId());

		// then
		verify(odsayClient, times(2)).searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble());
	}

	@Test
	@DisplayName("getRoutes - 경로가 없으면 빈 목록")
	void getRoutes_noRoute() {
		// given
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(OdsayFixture.TOO_CLOSE_JSON);

		// when
		TransitRouteResponse response = transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());

		// then
		assertThat(response.routes()).isEmpty();
	}

	@Test
	@DisplayName("getRoutes - 오디세이 호출이 실패하면 예외, 실패 결과는 캐시하지 않음")
	void getRoutes_apiFailed() {
		// given
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willThrow(new TransitApiException())
				.willReturn(OdsayFixture.SUCCESS_JSON);

		// when & then: 첫 번째는 실패
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId()))
				.isInstanceOf(TransitApiException.class);

		// 다시 조회하면 오디세이를 다시 호출해서 성공
		TransitRouteResponse retried = transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), gangnamStation.getId());
		assertThat(retried.routes()).hasSize(3);
		verify(odsayClient, times(2)).searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble());
	}

	@Test
	@DisplayName("getRoutes - 방문지에 위도 · 경도가 없으면 예외, 오디세이는 호출하지 않음")
	void getRoutes_noCoordinate() {
		// given
		Stop noLocation = savedStop(trip, "위치 없는 곳", null, null);

		// when & then
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), noLocation.getId()))
				.isInstanceOf(InvalidTransitRouteRequestException.class)
				.hasMessage("방문지의 위치(위도 · 경도)를 먼저 입력해주세요.");
		verify(odsayClient, never()).searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble());
	}

	@Test
	@DisplayName("getRoutes - 출발과 도착이 같은 방문지면 예외")
	void getRoutes_sameStop() {
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), seoulStation.getId()))
				.isInstanceOf(InvalidTransitRouteRequestException.class)
				.hasMessage("출발지와 도착지가 같습니다.");
	}

	@Test
	@DisplayName("getRoutes - 다른 여행의 방문지를 넣으면 예외")
	void getRoutes_stopOfOtherTrip() {
		// given: 같은 주인의 다른 여행
		Trip otherTrip = savedTrip(member);
		Stop otherStop = savedStop(otherTrip, "부산역", 35.1151, 129.0415);

		// when & then
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), otherStop.getId()))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("getRoutes - 남의 여행이면 예외")
	void getRoutes_othersTrip() {
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				"other@test.com", trip.getId(), seoulStation.getId(), gangnamStation.getId()))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("getRoutes - 존재하지 않는 여행 · 방문지면 예외")
	void getRoutes_notFound() {
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), NON_EXISTING_ID, seoulStation.getId(), gangnamStation.getId()))
				.isInstanceOf(TripNotFoundException.class);
		assertThatThrownBy(() -> transitRouteService.getRoutes(
				member.getEmail(), trip.getId(), seoulStation.getId(), NON_EXISTING_ID))
				.isInstanceOf(StopNotFoundException.class);
	}
}

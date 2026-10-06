package kr.ync.triplan.trip.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.client.OdsayClient;
import kr.ync.triplan.support.OdsayFixture;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.exception.TransitApiException;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import kr.ync.triplan.trip.service.TransitRouteCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransitRouteControllerTest extends BaseController {

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TransitRouteCache transitRouteCache;

	@MockitoBean
	private OdsayClient odsayClient;

	private Trip trip;
	private Stop seoulStation;
	private Stop gangnamStation;

	@BeforeEach
	void setUpFixture() {
		transitRouteCache.clear();
		trip = savedTrip(member);
		seoulStation = savedStop(trip, "서울역", 37.5547, 126.9707);
		gangnamStation = savedStop(trip, "강남역", 37.4979, 127.0276);
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

	private String routesUrl() {
		return "/api/trips/" + trip.getId() + "/transit-routes";
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 경로 후보와 소요시간 · 요금 · 구간")
	void getRoutes_endpoint() throws Exception {
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(OdsayFixture.SUCCESS_JSON);

		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", gangnamStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.routes.length()").value(3))
				.andExpect(jsonPath("$.routes[0].totalTime").value(33))
				.andExpect(jsonPath("$.routes[0].payment").value(1650))
				.andExpect(jsonPath("$.routes[0].transferCount").value(1))
				.andExpect(jsonPath("$.routes[0].walkDistance").value(227))
				.andExpect(jsonPath("$.routes[0].steps[1].type").value("SUBWAY"))
				.andExpect(jsonPath("$.routes[0].steps[1].name").value("수도권 4호선"))
				.andExpect(jsonPath("$.routes[0].steps[1].from").value("서울역"))
				.andExpect(jsonPath("$.routes[0].steps[1].to").value("사당"))
				.andExpect(jsonPath("$.routes[0].steps[0].type").value("WALK"));
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 경로가 없으면 빈 목록과 200")
	void getRoutes_endpoint_noRoute() throws Exception {
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(OdsayFixture.TOO_CLOSE_JSON);

		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", gangnamStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.routes.length()").value(0));
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 방문지에 위도 · 경도가 없으면 400")
	void getRoutes_endpoint_noCoordinate() throws Exception {
		Stop noLocation = savedStop(trip, "위치 없는 곳", null, null);

		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", noLocation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("방문지의 위치(위도 · 경도)를 먼저 입력해주세요."));
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 출발과 도착이 같으면 400")
	void getRoutes_endpoint_sameStop() throws Exception {
		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", seoulStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("출발지와 도착지가 같습니다."));
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 방문지 파라미터가 없으면 400")
	void getRoutes_endpoint_missingParam() throws Exception {
		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 다른 여행의 방문지면 403")
	void getRoutes_endpoint_stopOfOtherTrip() throws Exception {
		Stop otherStop = savedStop(savedTrip(member), "부산역", 35.1151, 129.0415);

		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", otherStop.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 남의 여행이면 403")
	void getRoutes_endpoint_othersTrip() throws Exception {
		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", gangnamStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(createMember("other@test.com"))))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 로그인 안 하면 401")
	void getRoutes_endpoint_unauthorized() throws Exception {
		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", gangnamStation.getId().toString()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transit-routes - 오디세이 호출이 실패하면 502")
	void getRoutes_endpoint_apiFailed() throws Exception {
		given(odsayClient.searchPubTransPath(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willThrow(new TransitApiException());

		mockMvc.perform(get(routesUrl())
						.param("fromStopId", seoulStation.getId().toString())
						.param("toStopId", gangnamStation.getId().toString())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.message").value("대중교통 정보를 불러오지 못했습니다."));
	}
}

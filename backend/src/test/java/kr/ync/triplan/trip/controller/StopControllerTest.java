package kr.ync.triplan.trip.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.StopCreateRequest;
import kr.ync.triplan.trip.dto.request.StopUpdateRequest;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StopControllerTest extends BaseController {

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TripRepository tripRepository;

	private Trip trip;

	@BeforeEach
	void setUpFixture() {
		trip = savedTrip(member);
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
	@DisplayName("POST /api/trips/{tripId}/stops - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("성산일출봉"))
				.andExpect(jsonPath("$.tripId").value(trip.getId()));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 2.필수 데이터 누락 (name 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "date": "%s"
				}
				""".formatted(LocalDate.now());

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("방문지 이름을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		StopCreateRequest request = new StopCreateRequest(null, null, null, null, null, null, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 4.비정상 데이터 (방문 순서 음수)")
	void create_endpoint_invalidData() throws Exception {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, -1, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("방문 순서는 0 이상이어야 합니다"));
	}

	// 위도 · 경도 · 주소만 바꿔서 요청을 보내는 헬퍼
	private ResultActions createWithLocation(
			Double latitude, Double longitude, String address) throws Exception {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1,
				latitude, longitude, address);

		return mockMvc.perform(
				post("/api/trips/{tripId}/stops", trip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 위도 · 경도 · 주소를 보내면 저장되고 응답에 나옴")
	void create_endpoint_withLocation() throws Exception {
		createWithLocation(33.4581, 126.9425, "제주 서귀포시 성산읍")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.latitude").value(33.4581))
				.andExpect(jsonPath("$.longitude").value(126.9425))
				.andExpect(jsonPath("$.address").value("제주 서귀포시 성산읍"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 위도 · 경도 · 주소 없이도 생성됨 (값은 null)")
	void create_endpoint_withoutLocation() throws Exception {
		createWithLocation(null, null, null)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.latitude").isEmpty())
				.andExpect(jsonPath("$.longitude").isEmpty())
				.andExpect(jsonPath("$.address").isEmpty());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 위도나 경도 하나만 보내면 400")
	void create_endpoint_onlyOneCoordinate() throws Exception {
		createWithLocation(33.4581, null, null)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("위도와 경도는 함께 입력해주세요"));

		createWithLocation(null, 126.9425, null)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("위도와 경도는 함께 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 위도 · 경도가 범위를 벗어나면 400")
	void create_endpoint_coordinateOutOfRange() throws Exception {
		createWithLocation(90.1, 126.9425, null)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("위도는 -90 ~ 90 사이여야 합니다"));

		createWithLocation(33.4581, -180.1, null)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("경도는 -180 ~ 180 사이여야 합니다"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 주소가 255자를 넘으면 400")
	void create_endpoint_addressTooLong() throws Exception {
		createWithLocation(null, null, "가".repeat(256))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("주소는 255자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("PUT /api/stops/{id} - 위도 · 경도 · 주소 수정")
	void update_endpoint_withLocation() throws Exception {
		Stop saved = savedStop("성산일출봉", 1);
		StopUpdateRequest request = new StopUpdateRequest(
				"성산일출봉", LocalDate.now(), null, null, null, 1, 33.4581, 126.9425, "제주 서귀포시 성산읍");

		mockMvc.perform(put("/api/stops/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.latitude").value(33.4581))
				.andExpect(jsonPath("$.longitude").value(126.9425))
				.andExpect(jsonPath("$.address").value("제주 서귀포시 성산읍"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 존재하지 않는 여행이면 404")
	void create_endpoint_tripNotFound() throws Exception {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", 99999L)
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 남의 여행이면 403")
	void create_endpoint_forbidden() throws Exception {
		Trip othersTrip = savedTrip(createMember("other@test.com"));
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", othersTrip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/stops - 순서대로 조회")
	void list_endpoint() throws Exception {
		savedStop("두번째", 2);
		savedStop("첫번째", 1);

		mockMvc.perform(get("/api/trips/{tripId}/stops", trip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("첫번째"))
				.andExpect(jsonPath("$[1].name").value("두번째"));
	}

	@Test
	@DisplayName("GET /api/stops/{id}")
	void get_endpoint() throws Exception {
		Stop saved = savedStop("성산일출봉", 1);

		mockMvc.perform(get("/api/stops/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.name").value("성산일출봉"));
	}

	@Test
	@DisplayName("GET /api/stops/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/stops/{id}", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("PUT /api/stops/{id}")
	void update_endpoint() throws Exception {
		Stop saved = savedStop("원래 이름", 1);
		StopUpdateRequest request = new StopUpdateRequest(
				"변경된 이름", LocalDate.now(), LocalTime.of(10, 0), "메모", null, 2, null, null, null);

		mockMvc.perform(put("/api/stops/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("변경된 이름"));
	}

	@Test
	@DisplayName("DELETE /api/stops/{id}")
	void delete_endpoint() throws Exception {
		Stop saved = savedStop("성산일출봉", 1);

		mockMvc.perform(delete("/api/stops/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/stops/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}
}

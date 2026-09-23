package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.StopCreateRequest;
import kr.ync.triplan.dto.request.StopUpdateRequest;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);

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
		StopCreateRequest request = new StopCreateRequest(null, null, null, null, null, null);

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
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, -1);

		mockMvc.perform(
						post("/api/trips/{tripId}/stops", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("방문 순서는 0 이상이어야 합니다"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/stops - 존재하지 않는 여행이면 404")
	void create_endpoint_tripNotFound() throws Exception {
		StopCreateRequest request = new StopCreateRequest(
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);

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
				"성산일출봉", LocalDate.now(), LocalTime.of(9, 0), "일출 명소", null, 1);

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
				"변경된 이름", LocalDate.now(), LocalTime.of(10, 0), "메모", null, 2);

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

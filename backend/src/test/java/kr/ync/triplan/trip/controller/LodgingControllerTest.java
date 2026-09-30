package kr.ync.triplan.trip.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.LodgingCreateRequest;
import kr.ync.triplan.trip.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LodgingControllerTest extends BaseController {

	@Autowired
	private LodgingRepository lodgingRepository;

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

	private Lodging savedLodging(String name) {
		return lodgingRepository.save(
				Lodging.builder()
						.trip(trip)
						.name(name)
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/lodgings - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		LodgingCreateRequest request = new LodgingCreateRequest(
				"제주 호텔", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 100000, "RES123");

		mockMvc.perform(
						post("/api/trips/{tripId}/lodgings", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("제주 호텔"))
				.andExpect(jsonPath("$.tripId").value(trip.getId()));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/lodgings - 2.필수 데이터 누락 (name 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "checkIn": "%s",
				  "checkOut": "%s"
				}
				""".formatted(LocalDateTime.now(), LocalDateTime.now().plusDays(1));

		mockMvc.perform(
						post("/api/trips/{tripId}/lodgings", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("숙소 이름을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/lodgings - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		LodgingCreateRequest request = new LodgingCreateRequest(null, null, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/lodgings", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/lodgings - 4.비정상 데이터 (체크아웃이 체크인보다 빠름)")
	void create_endpoint_invalidData() throws Exception {
		LodgingCreateRequest request = new LodgingCreateRequest(
				"제주 호텔", LocalDateTime.now(), LocalDateTime.now().minusDays(1), null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/lodgings", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("체크아웃 시간은 체크인 시간보다 빠를 수 없습니다"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/lodgings - 존재하지 않는 여행이면 404")
	void create_endpoint_tripNotFound() throws Exception {
		LodgingCreateRequest request = new LodgingCreateRequest(
				"제주 호텔", LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/lodgings", 99999L)
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/lodgings/{id} - 남의 숙소면 403")
	void get_endpoint_forbidden() throws Exception {
		Lodging saved = savedLodging("제주 호텔");

		mockMvc.perform(get("/api/lodgings/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(createMember("other@test.com"))))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/lodgings")
	void list_endpoint() throws Exception {
		savedLodging("제주 호텔");
		savedLodging("서귀포 펜션");

		mockMvc.perform(get("/api/trips/{tripId}/lodgings", trip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("GET /api/lodgings/{id}")
	void get_endpoint() throws Exception {
		Lodging saved = savedLodging("제주 호텔");

		mockMvc.perform(get("/api/lodgings/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.name").value("제주 호텔"));
	}

	@Test
	@DisplayName("GET /api/lodgings/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/lodgings/{id}", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("PUT /api/lodgings/{id}")
	void update_endpoint() throws Exception {
		Lodging saved = savedLodging("원래 이름");
		LodgingUpdateRequest request = new LodgingUpdateRequest(
				"변경된 이름", LocalDateTime.now(), LocalDateTime.now().plusDays(2), 200000, "RES999");

		mockMvc.perform(put("/api/lodgings/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("변경된 이름"));
	}

	@Test
	@DisplayName("DELETE /api/lodgings/{id}")
	void delete_endpoint() throws Exception {
		Lodging saved = savedLodging("제주 호텔");

		mockMvc.perform(delete("/api/lodgings/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/lodgings/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}
}

package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripControllerTest extends BaseController {

	@Autowired
	private TripRepository tripRepository;

	private Trip savedTrip(String title, String userId) {
		return tripRepository.save(
				Trip.builder()
						.title(title)
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId(userId)
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/trips - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().plusDays(3), "user1");

		mockMvc.perform(
						post("/api/trips")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("제주도 여행"))
				.andExpect(jsonPath("$.userId").value("user1"));
	}

	@Test
	@DisplayName("POST /api/trips - 2.필수 데이터 누락 (title 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "startDate": "%s",
				  "endDate": "%s",
				  "userId": "user1"
				}
				""".formatted(LocalDate.now(), LocalDate.now().plusDays(3));

		mockMvc.perform(
						post("/api/trips")
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("여행 제목을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		TripCreateRequest request = new TripCreateRequest(null, null, null, null);

		mockMvc.perform(
						post("/api/trips")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/trips - 4.비정상 데이터 (종료일이 시작일보다 빠름)")
	void create_endpoint_invalidData() throws Exception {
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().minusDays(1), "user1");

		mockMvc.perform(
						post("/api/trips")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다"));
	}

	@Test
	@DisplayName("GET /api/trips/{id}")
	void get_endpoint() throws Exception {
		Trip saved = savedTrip("제주도 여행", "user1");

		mockMvc.perform(get("/api/trips/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.title").value("제주도 여행"));
	}

	@Test
	@DisplayName("GET /api/trips/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/trips/{id}", 99999L))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/trips?userId= - 본인 여행만 조회")
	void list_endpoint() throws Exception {
		savedTrip("제주도 여행", "user1");
		savedTrip("부산 여행", "user1");
		savedTrip("서울 여행", "user2");

		mockMvc.perform(get("/api/trips").param("userId", "user1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("PUT /api/trips/{id}")
	void update_endpoint() throws Exception {
		Trip saved = savedTrip("원래 제목", "user1");
		TripUpdateRequest request = new TripUpdateRequest(
				"변경된 제목", LocalDate.now(), LocalDate.now().plusDays(5));

		mockMvc.perform(put("/api/trips/{id}", saved.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("변경된 제목"));
	}

	@Test
	@DisplayName("DELETE /api/trips/{id}")
	void delete_endpoint() throws Exception {
		Trip saved = savedTrip("제목", "user1");

		mockMvc.perform(delete("/api/trips/{id}", saved.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/trips/{id}", saved.getId()))
				.andExpect(status().isNotFound());
	}
}

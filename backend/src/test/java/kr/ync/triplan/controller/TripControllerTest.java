package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Lodging;
import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.TransportMode;
import kr.ync.triplan.domain.TransportSegment;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.repository.LodgingRepository;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TransportSegmentRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TripControllerTest extends BaseController {

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private LodgingRepository lodgingRepository;

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
	@DisplayName("POST /api/trips - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().plusDays(3));

		mockMvc.perform(
						post("/api/trips")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("제주도 여행"));
	}

	@Test
	@DisplayName("POST /api/trips - 2.필수 데이터 누락 (title 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "startDate": "%s",
				  "endDate": "%s"
				}
				""".formatted(LocalDate.now(), LocalDate.now().plusDays(3));

		mockMvc.perform(
						post("/api/trips")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("여행 제목을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		TripCreateRequest request = new TripCreateRequest(null, null, null);

		mockMvc.perform(
						post("/api/trips")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/trips - 4.비정상 데이터 (종료일이 시작일보다 빠름)")
	void create_endpoint_invalidData() throws Exception {
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().minusDays(1));

		mockMvc.perform(
						post("/api/trips")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다"));
	}

	@Test
	@DisplayName("POST /api/trips - 로그인 안 하면 401")
	void create_endpoint_unauthorized() throws Exception {
		TripCreateRequest request = new TripCreateRequest(
				"제주도 여행", LocalDate.now(), LocalDate.now().plusDays(3));

		mockMvc.perform(
						post("/api/trips")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("GET /api/trips/{id}")
	void get_endpoint() throws Exception {
		Trip saved = savedTrip("제주도 여행", member);

		mockMvc.perform(get("/api/trips/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.title").value("제주도 여행"));
	}

	@Test
	@DisplayName("GET /api/trips/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/trips/{id}", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/trips/{id} - 남의 여행이면 403")
	void get_endpoint_forbidden() throws Exception {
		Member other = createMember("other@test.com");
		Trip othersTrip = savedTrip("남의 여행", other);

		mockMvc.perform(get("/api/trips/{id}", othersTrip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
	}

	@Test
	@DisplayName("GET /api/trips/{id}/estimate - 교통비/숙박비 합산")
	void getEstimate_endpoint() throws Exception {
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

		mockMvc.perform(get("/api/trips/{id}/estimate", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transportCost").value(20000))
				.andExpect(jsonPath("$.lodgingCost").value(100000))
				.andExpect(jsonPath("$.totalCost").value(120000));
	}

	@Test
	@DisplayName("GET /api/trips/{id}/estimate - 존재하지 않으면 404")
	void getEstimate_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/trips/{id}/estimate", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/trips - 본인 여행만 조회")
	void list_endpoint() throws Exception {
		Member other = createMember("other@test.com");
		savedTrip("제주도 여행", member);
		savedTrip("부산 여행", member);
		savedTrip("남의 여행", other);

		mockMvc.perform(get("/api/trips")
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("PUT /api/trips/{id}")
	void update_endpoint() throws Exception {
		Trip saved = savedTrip("원래 제목", member);
		TripUpdateRequest request = new TripUpdateRequest(
				"변경된 제목", LocalDate.now(), LocalDate.now().plusDays(5));

		mockMvc.perform(put("/api/trips/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("변경된 제목"));
	}

	@Test
	@DisplayName("DELETE /api/trips/{id}")
	void delete_endpoint() throws Exception {
		Trip saved = savedTrip("제목", member);

		mockMvc.perform(delete("/api/trips/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/trips/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /api/trips/{id} - 남의 여행이면 403")
	void delete_endpoint_forbidden() throws Exception {
		Member other = createMember("other@test.com");
		Trip othersTrip = savedTrip("남의 여행", other);

		mockMvc.perform(delete("/api/trips/{id}", othersTrip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isForbidden());
	}
}

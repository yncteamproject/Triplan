package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.TransportMode;
import kr.ync.triplan.domain.TransportSegment;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TransportSegmentRepository;
import kr.ync.triplan.repository.TripRepository;
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

class TransportSegmentControllerTest extends BaseController {

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	private Trip trip;
	private Stop fromStop;
	private Stop toStop;

	@BeforeEach
	void setUpFixture() {
		trip = savedTrip(member);
		fromStop = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).build());
		toStop = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).build());
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

	private TransportSegment savedSegment() {
		return transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip)
						.fromStop(fromStop)
						.toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), 5000, "RES123");

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.mode").value("CAR"))
				.andExpect(jsonPath("$.tripId").value(trip.getId()));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 2.필수 데이터 누락 (mode 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "fromStopId": %d,
				  "toStopId": %d,
				  "departTime": "%s",
				  "arriveTime": "%s"
				}
				""".formatted(fromStop.getId(), toStop.getId(), LocalDateTime.now(), LocalDateTime.now().plusHours(1));

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("이동 수단을 선택해주세요"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				null, null, null, null, null, null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 4.비정상 데이터 (도착시간이 출발시간보다 빠름)")
	void create_endpoint_invalidData() throws Exception {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().minusHours(1), null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("도착 시간은 출발 시간보다 빠를 수 없습니다"));
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 존재하지 않는 방문지면 404")
	void create_endpoint_stopNotFound() throws Exception {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				99999L, toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/trips/{tripId}/transport-segments - 남의 방문지를 쓰면 403")
	void create_endpoint_othersStopForbidden() throws Exception {
		Trip othersTrip = savedTrip(createMember("other@test.com"));
		Stop othersStop = stopRepository.save(
				Stop.builder().trip(othersTrip).name("남의 장소").date(LocalDate.now()).build());
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				fromStop.getId(), othersStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null);

		mockMvc.perform(
						post("/api/trips/{tripId}/transport-segments", trip.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/trips/{tripId}/transport-segments")
	void list_endpoint() throws Exception {
		savedSegment();
		savedSegment();

		mockMvc.perform(get("/api/trips/{tripId}/transport-segments", trip.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("GET /api/transport-segments/{id}")
	void get_endpoint() throws Exception {
		TransportSegment saved = savedSegment();

		mockMvc.perform(get("/api/transport-segments/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()));
	}

	@Test
	@DisplayName("GET /api/transport-segments/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/transport-segments/{id}", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("PUT /api/transport-segments/{id}")
	void update_endpoint() throws Exception {
		TransportSegment saved = savedSegment();
		TransportSegmentUpdateRequest request = new TransportSegmentUpdateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.FLIGHT,
				LocalDateTime.now(), LocalDateTime.now().plusHours(2), 100000, "RES999");

		mockMvc.perform(put("/api/transport-segments/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mode").value("FLIGHT"));
	}

	@Test
	@DisplayName("DELETE /api/transport-segments/{id}")
	void delete_endpoint() throws Exception {
		TransportSegment saved = savedSegment();

		mockMvc.perform(delete("/api/transport-segments/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/transport-segments/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}
}

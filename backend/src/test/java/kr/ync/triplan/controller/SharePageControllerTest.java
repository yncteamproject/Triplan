package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.SharePageCreateRequest;
import kr.ync.triplan.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.repository.SharePageRepository;
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

class SharePageControllerTest extends BaseController {

	@Autowired
	private SharePageRepository sharePageRepository;

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

	private SharePage savedSharePage(String title, String description) {
		return sharePageRepository.save(
				SharePage.builder()
						.title(title)
						.description(description)
						.trip(trip)
						.writer(member)
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/share-pages - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", trip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("제목"))
				.andExpect(jsonPath("$.description").value("설명"))
				.andExpect(jsonPath("$.tripId").value(trip.getId()))
				.andExpect(jsonPath("$.writerId").value(member.getId()))
				.andExpect(jsonPath("$.writerNickname").value(member.getNickname()));
	}

	@Test
	@DisplayName("POST /api/share-pages - 2.필수 데이터 누락 (title 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "description": "설명",
				  "tripId": %d
				}
				""".formatted(trip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("제목을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		SharePageCreateRequest request = new SharePageCreateRequest(null, null, null);

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/share-pages - 4.비정상 데이터 (제목 100자 초과)")
	void create_endpoint_invalidData() throws Exception {
		String tooLongTitle = "가".repeat(101);
		SharePageCreateRequest request = new SharePageCreateRequest(tooLongTitle, "설명", trip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("제목은 100자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages - 존재하지 않는 여행이면 404")
	void create_endpoint_tripNotFound() throws Exception {
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", 99999L);

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/share-pages - 남의 여행을 공유하면 403")
	void create_endpoint_othersTripForbidden() throws Exception {
		Trip othersTrip = savedTrip(createMember("other@test.com"));
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", othersTrip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("POST /api/share-pages - 로그인 안 하면 401")
	void create_endpoint_unauthorized() throws Exception {
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", trip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /api/share-pages/{id} - 비로그인도 조회 가능")
	void get_endpoint() throws Exception {
		SharePage saved = savedSharePage("제목", "내용");

		mockMvc.perform(get("/api/share-pages/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.title").value(saved.getTitle()))
				.andExpect(jsonPath("$.viewCount").value(1));
	}

	@Test
	@DisplayName("GET /api/share-pages/{id} - 존재하지 않으면 404")
	void get_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/share-pages/{id}", 99999L))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/share-pages - 비로그인도 조회 가능")
	void list_endpoint() throws Exception {
		savedSharePage("t1", "c1");
		savedSharePage("t2", "c2");
		savedSharePage("t3", "c3");

		mockMvc.perform(get("/api/share-pages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	@DisplayName("PUT /api/share-pages/{id}")
	void update_endpoint() throws Exception {
		SharePage saved = savedSharePage("원본 제목", "원본 내용");
		SharePageUpdateRequest request = new SharePageUpdateRequest("변경 제목", "변경 내용");

		mockMvc.perform(put("/api/share-pages/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.title").value("변경 제목"))
				.andExpect(jsonPath("$.description").value("변경 내용"));
	}

	@Test
	@DisplayName("PUT /api/share-pages/{id} - 작성자가 아니면 403")
	void update_endpoint_forbidden() throws Exception {
		SharePage saved = savedSharePage("원본 제목", "원본 내용");
		SharePageUpdateRequest request = new SharePageUpdateRequest("변경 제목", "변경 내용");

		mockMvc.perform(put("/api/share-pages/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(createMember("other@test.com")))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("DELETE /api/share-pages/{id}")
	void delete_endpoint() throws Exception {
		SharePage saved = savedSharePage("제목", "내용");

		mockMvc.perform(delete("/api/share-pages/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/share-pages/{id}", saved.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /api/share-pages/{id} - 작성자가 아니면 403")
	void delete_endpoint_forbidden() throws Exception {
		SharePage saved = savedSharePage("제목", "내용");

		mockMvc.perform(delete("/api/share-pages/{id}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(createMember("other@test.com"))))
				.andExpect(status().isForbidden());
	}
}

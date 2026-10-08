package kr.ync.triplan.share.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.request.TripCopyRequest;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportMode;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
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

class SharePageControllerTest extends BaseController {

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private LodgingRepository lodgingRepository;

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
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", trip.getId(), null);

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
		SharePageCreateRequest request = new SharePageCreateRequest(null, null, null, null);

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
		SharePageCreateRequest request = new SharePageCreateRequest(tooLongTitle, "설명", trip.getId(), null);

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("제목은 100자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages - 설명 2000자(최대 길이)도 저장됨")
	void create_endpoint_maxLengthDescription() throws Exception {
		String maxDescription = "가".repeat(2000);
		SharePageCreateRequest request = new SharePageCreateRequest("제목", maxDescription, trip.getId(), null);

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value(maxDescription));
	}

	@Test
	@DisplayName("POST /api/share-pages - 존재하지 않는 여행이면 404")
	void create_endpoint_tripNotFound() throws Exception {
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", 99999L, null);

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
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", othersTrip.getId(), null);

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
		SharePageCreateRequest request = new SharePageCreateRequest("제목", "설명", trip.getId(), null);

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
				.andExpect(jsonPath("$.content.length()").value(3))
				.andExpect(jsonPath("$.content[0].title").value("t3"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	@DisplayName("GET /api/share-pages?page=1&size=2 - 다음 페이지 조회")
	void list_endpoint_secondPage() throws Exception {
		savedSharePage("t1", "c1");
		savedSharePage("t2", "c2");
		savedSharePage("t3", "c3");

		mockMvc.perform(get("/api/share-pages").param("page", "1").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].title").value("t1"))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	@DisplayName("GET /api/share-pages - 범위를 벗어난 페이지는 빈 목록과 200")
	void list_endpoint_pageOutOfRange() throws Exception {
		savedSharePage("t1", "c1");

		mockMvc.perform(get("/api/share-pages").param("page", "9"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("GET /api/share-pages - size가 50보다 크면 50으로 제한")
	void list_endpoint_sizeCapped() throws Exception {
		mockMvc.perform(get("/api/share-pages").param("size", "1000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(50));
	}

	@Test
	@DisplayName("GET /api/share-pages - page가 음수이거나 size가 0 이하이면 400")
	void list_endpoint_invalidPageRequest() throws Exception {
		mockMvc.perform(get("/api/share-pages").param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("page는 0 이상, size는 1 이상이어야 합니다."));

		mockMvc.perform(get("/api/share-pages").param("size", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/share-pages/{id}")
	void update_endpoint() throws Exception {
		SharePage saved = savedSharePage("원본 제목", "원본 내용");
		SharePageUpdateRequest request = new SharePageUpdateRequest("변경 제목", "변경 내용", null);

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
		SharePageUpdateRequest request = new SharePageUpdateRequest("변경 제목", "변경 내용", null);

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

	@Test
	@DisplayName("GET /api/share-pages/{id}/trip - 비로그인도 조회 가능, 예약번호는 공개하지 않음")
	void getSharedTrip_endpoint() throws Exception {
		SharePage saved = savedSharePage("제주도 후기", "내용");
		Stop fromStop = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).stopOrder(1).build());
		Stop toStop = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).stopOrder(2).build());
		transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip).fromStop(fromStop).toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.cost(20000).reservationNo("RES-SECRET")
						.build()
		);
		lodgingRepository.save(
				Lodging.builder()
						.trip(trip).name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.cost(100000).reservationNo("RES-SECRET")
						.build()
		);

		mockMvc.perform(get("/api/share-pages/{id}/trip", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("제주도 여행"))
				.andExpect(jsonPath("$.stops[0].name").value("공항"))
				.andExpect(jsonPath("$.stops[1].name").value("숙소"))
				.andExpect(jsonPath("$.transportSegments[0].mode").value("CAR"))
				.andExpect(jsonPath("$.transportSegments[0].reservationNo").doesNotExist())
				.andExpect(jsonPath("$.lodgings[0].name").value("제주 호텔"))
				.andExpect(jsonPath("$.lodgings[0].reservationNo").doesNotExist())
				.andExpect(jsonPath("$.totalCost").value(120000));
	}

	@Test
	@DisplayName("GET /api/share-pages/{id}/trip - 존재하지 않으면 404")
	void getSharedTrip_endpoint_notFound() throws Exception {
		mockMvc.perform(get("/api/share-pages/{id}/trip", 99999L))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/share-pages - allowCopy를 안 보내면 true, false로 보내면 false")
	void create_endpoint_allowCopy() throws Exception {
		String defaultJson = """
				{ "title": "제목", "tripId": %d }
				""".formatted(trip.getId());
		String notAllowedJson = """
				{ "title": "제목", "tripId": %d, "allowCopy": false }
				""".formatted(trip.getId());

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(defaultJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.allowCopy").value(true))
				.andExpect(jsonPath("$.copyCount").value(0));

		mockMvc.perform(
						post("/api/share-pages")
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(notAllowedJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.allowCopy").value(false));
	}

	@Test
	@DisplayName("GET /api/share-pages, /api/share-pages/{id} - 응답에 allowCopy · copyCount 포함")
	void get_endpoint_allowCopyAndCopyCount() throws Exception {
		SharePage saved = savedSharePage("제목", "내용");

		mockMvc.perform(get("/api/share-pages/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowCopy").value(true))
				.andExpect(jsonPath("$.copyCount").value(0));

		mockMvc.perform(get("/api/share-pages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].allowCopy").value(true))
				.andExpect(jsonPath("$.content[0].copyCount").value(0));
	}

	@Test
	@DisplayName("PUT /api/share-pages/{id} - allowCopy 변경")
	void update_endpoint_allowCopy() throws Exception {
		SharePage saved = savedSharePage("제목", "내용");
		SharePageUpdateRequest request = new SharePageUpdateRequest("제목", "내용", false);

		mockMvc.perform(
						put("/api/share-pages/{id}", saved.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowCopy").value(false));
	}

	@Test
	@DisplayName("POST /api/share-pages/{id}/copy - 본문 없이 복사하면 원본 날짜 그대로 내 여행이 생김")
	void copy_endpoint() throws Exception {
		SharePage saved = savedSharePage("제주도 후기", "내용");
		Member copier = createMember("copier@test.com");

		mockMvc.perform(
						post("/api/share-pages/{id}/copy", saved.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(copier)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("제주도 여행"))
				.andExpect(jsonPath("$.startDate").value(trip.getStartDate().toString()))
				.andExpect(jsonPath("$.endDate").value(trip.getEndDate().toString()));

		// 복사한 여행은 복사한 사람의 여행 목록에 나오고, 게시글의 복사 수가 오른다
		mockMvc.perform(get("/api/trips").header(HttpHeaders.AUTHORIZATION, bearer(copier)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(get("/api/share-pages/{id}", saved.getId()))
				.andExpect(jsonPath("$.copyCount").value(1));
	}

	@Test
	@DisplayName("POST /api/share-pages/{id}/copy - startDate를 보내면 그 날짜로 옮겨서 복사")
	void copy_endpoint_withStartDate() throws Exception {
		SharePage saved = savedSharePage("제주도 후기", "내용");
		LocalDate newStartDate = trip.getStartDate().plusDays(30);
		TripCopyRequest request = new TripCopyRequest(newStartDate);

		mockMvc.perform(
						post("/api/share-pages/{id}/copy", saved.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(createMember("copier@test.com")))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.startDate").value(newStartDate.toString()))
				.andExpect(jsonPath("$.endDate").value(trip.getEndDate().plusDays(30).toString()));
	}

	@Test
	@DisplayName("POST /api/share-pages/{id}/copy - 로그인 안 하면 401")
	void copy_endpoint_unauthorized() throws Exception {
		SharePage saved = savedSharePage("제주도 후기", "내용");

		mockMvc.perform(post("/api/share-pages/{id}/copy", saved.getId()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("POST /api/share-pages/{id}/copy - 복사를 허용하지 않은 게시글을 다른 사람이 복사하면 403")
	void copy_endpoint_forbidden() throws Exception {
		SharePage saved = savedSharePage("제주도 후기", "내용");
		saved.setAllowCopy(false);

		mockMvc.perform(
						post("/api/share-pages/{id}/copy", saved.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(createMember("copier@test.com"))))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("POST /api/share-pages/{id}/copy - 존재하지 않으면 404")
	void copy_endpoint_notFound() throws Exception {
		mockMvc.perform(
						post("/api/share-pages/{id}/copy", 99999L)
								.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/share-pages - 목록에 여행 요약(기간 · 지역 · 방문지 수 · 총 경비) 포함")
	void list_endpoint_tripSummary() throws Exception {
		savedSharePage("제주도 후기", "내용");
		stopRepository.save(
				Stop.builder().trip(trip).name("성산일출봉").date(LocalDate.now()).stopOrder(1)
						.address("제주특별자치도 서귀포시 성산읍").build());
		lodgingRepository.save(
				Lodging.builder()
						.trip(trip).name("제주 호텔")
						.checkIn(LocalDateTime.now())
						.checkOut(LocalDateTime.now().plusDays(1))
						.cost(100000)
						.build()
		);

		mockMvc.perform(get("/api/share-pages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].tripStartDate").value(trip.getStartDate().toString()))
				.andExpect(jsonPath("$.content[0].tripEndDate").value(trip.getEndDate().toString()))
				.andExpect(jsonPath("$.content[0].region").value("제주"))
				.andExpect(jsonPath("$.content[0].stopCount").value(1))
				.andExpect(jsonPath("$.content[0].totalCost").value(100000));
	}

	@Test
	@DisplayName("GET /api/share-pages - 방문지 · 비용이 없으면 0, 주소가 없으면 region은 null")
	void list_endpoint_tripSummary_empty() throws Exception {
		savedSharePage("빈 여행", "내용");

		mockMvc.perform(get("/api/share-pages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].region").isEmpty())
				.andExpect(jsonPath("$.content[0].stopCount").value(0))
				.andExpect(jsonPath("$.content[0].totalCost").value(0));
	}
}

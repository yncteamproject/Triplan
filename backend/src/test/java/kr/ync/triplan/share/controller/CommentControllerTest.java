package kr.ync.triplan.share.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.CommentCreateRequest;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.domain.Trip;
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

class CommentControllerTest extends BaseController {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private TripRepository tripRepository;

	private SharePage sharePage;

	@BeforeEach
	void setUpFixture() {
		Trip trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.member(member)
						.build()
		);
		sharePage = sharePageRepository.save(
				SharePage.builder()
						.title("제주도 3박 4일")
						.description("여행 후기")
						.trip(trip)
						.writer(member)
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
	}

	private Comment savedComment(String content, Member writer) {
		return commentRepository.save(
				Comment.builder()
						.content(content)
						.sharePage(sharePage)
						.writer(writer)
						.createdAt(LocalDateTime.now())
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("좋은 정보 감사합니다");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.content").value("좋은 정보 감사합니다"))
				.andExpect(jsonPath("$.writerId").value(member.getId()));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 2.필수 데이터 누락 (content 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("댓글 내용을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest(null);

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 4.비정상 데이터 (내용 500자 초과)")
	void create_endpoint_invalidData() throws Exception {
		String tooLongContent = "가".repeat(501);
		CommentCreateRequest request = new CommentCreateRequest(tooLongContent);

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("댓글은 500자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 내용 500자(최대 길이)도 저장됨")
	void create_endpoint_maxLengthContent() throws Exception {
		String maxContent = "가".repeat(500);
		CommentCreateRequest request = new CommentCreateRequest(maxContent);

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value(maxContent));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 존재하지 않는 게시글이면 404")
	void create_endpoint_sharePageNotFound() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("내용");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", 99999L)
								.header(HttpHeaders.AUTHORIZATION, bearer(member))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 로그인 안 하면 401")
	void create_endpoint_unauthorized() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("내용");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /api/share-pages/{sharePageId}/comments - 비로그인도 조회 가능")
	void list_endpoint() throws Exception {
		savedComment("댓글1", member);
		savedComment("댓글2", member);

		mockMvc.perform(get("/api/share-pages/{sharePageId}/comments", sharePage.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("DELETE /api/comments/{commentId}")
	void delete_endpoint() throws Exception {
		Comment saved = savedComment("댓글", member);

		mockMvc.perform(delete("/api/comments/{commentId}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /api/comments/{commentId} - 존재하지 않으면 404")
	void delete_endpoint_notFound() throws Exception {
		mockMvc.perform(delete("/api/comments/{commentId}", 99999L)
						.header(HttpHeaders.AUTHORIZATION, bearer(member)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /api/comments/{commentId} - 작성자가 아니면 403")
	void delete_endpoint_forbidden() throws Exception {
		Comment saved = savedComment("댓글", member);

		mockMvc.perform(delete("/api/comments/{commentId}", saved.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(createMember("other@test.com"))))
				.andExpect(status().isForbidden());
	}
}

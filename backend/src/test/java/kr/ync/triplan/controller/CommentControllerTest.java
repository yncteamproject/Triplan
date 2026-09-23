package kr.ync.triplan.controller;

import kr.ync.triplan.domain.Comment;
import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.repository.CommentRepository;
import kr.ync.triplan.repository.SharePageRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
						.userId("user1")
						.build()
		);
		sharePage = sharePageRepository.save(
				SharePage.builder()
						.title("제주도 3박 4일")
						.description("여행 후기")
						.trip(trip)
						.writerId("user1")
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
	}

	private Comment savedComment(String content, String writerId) {
		return commentRepository.save(
				Comment.builder()
						.content(content)
						.sharePage(sharePage)
						.writerId(writerId)
						.createdAt(LocalDateTime.now())
						.build()
		);
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 1.정상 데이터")
	void create_endpoint_validData() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("좋은 정보 감사합니다", "user2");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.content").value("좋은 정보 감사합니다"))
				.andExpect(jsonPath("$.writerId").value("user2"));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 2.필수 데이터 누락 (content 키 자체 없음)")
	void create_endpoint_missingRequiredField() throws Exception {
		String json = """
				{
				  "writerId": "user2"
				}
				""";

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("댓글 내용을 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 3.null 값 (모든 필드 null)")
	void create_endpoint_nullValues() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest(null, null);

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 4.비정상 데이터 (내용 500자 초과)")
	void create_endpoint_invalidData() throws Exception {
		String tooLongContent = "가".repeat(501);
		CommentCreateRequest request = new CommentCreateRequest(tooLongContent, "user2");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", sharePage.getId())
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("댓글은 500자 이내로 입력해주세요"));
	}

	@Test
	@DisplayName("POST /api/share-pages/{sharePageId}/comments - 존재하지 않는 게시글이면 404")
	void create_endpoint_sharePageNotFound() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("내용", "user2");

		mockMvc.perform(
						post("/api/share-pages/{sharePageId}/comments", 99999L)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/share-pages/{sharePageId}/comments")
	void list_endpoint() throws Exception {
		savedComment("댓글1", "user2");
		savedComment("댓글2", "user3");

		mockMvc.perform(get("/api/share-pages/{sharePageId}/comments", sharePage.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("DELETE /api/comments/{commentId}")
	void delete_endpoint() throws Exception {
		Comment saved = savedComment("댓글", "user2");

		mockMvc.perform(delete("/api/comments/{commentId}", saved.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /api/comments/{commentId} - 존재하지 않으면 404")
	void delete_endpoint_notFound() throws Exception {
		mockMvc.perform(delete("/api/comments/{commentId}", 99999L))
				.andExpect(status().isNotFound());
	}
}

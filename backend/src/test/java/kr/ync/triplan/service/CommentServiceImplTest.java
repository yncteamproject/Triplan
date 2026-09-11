package kr.ync.triplan.service;

import kr.ync.triplan.domain.Comment;
import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.dto.response.CommentResponse;
import kr.ync.triplan.exception.CommentNotFoundException;
import kr.ync.triplan.exception.SharePageNotFoundException;
import kr.ync.triplan.repository.CommentRepository;
import kr.ync.triplan.repository.SharePageRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class CommentServiceImplTest {

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private TripRepository tripRepository;

	private static final long NON_EXISTING_ID = 99999L;

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
	@DisplayName("create")
	void create_success() {
		// given
		CommentCreateRequest request = new CommentCreateRequest("좋은 정보 감사합니다", "user2");
		// when
		CommentResponse response = commentService.create(sharePage.getId(), request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(CommentResponse::content, CommentResponse::writerId)
				.containsExactly("좋은 정보 감사합니다", "user2");
	}

	@Test
	@DisplayName("create - 존재하지 않는 게시글이면 예외")
	void create_sharePageNotFound() {
		// given
		CommentCreateRequest request = new CommentCreateRequest("내용", "user2");
		// when & then
		assertThatThrownBy(() -> commentService.create(NON_EXISTING_ID, request))
				.isInstanceOf(SharePageNotFoundException.class);
	}

	@Test
	@DisplayName("list - 등록순 목록조회")
	void list_success() {
		// given
		savedComment("댓글1", "user2");
		savedComment("댓글2", "user3");
		// when
		List<CommentResponse> list = commentService.getList(sharePage.getId());
		// then
		assertThat(list).hasSize(2)
				.extracting(CommentResponse::content)
				.containsExactly("댓글1", "댓글2");
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		Comment saved = savedComment("댓글", "user2");
		// when
		commentService.delete(saved.getId());
		// then
		assertThat(commentRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> commentService.delete(NON_EXISTING_ID))
				.isInstanceOf(CommentNotFoundException.class);
	}
}

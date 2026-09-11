package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Comment;
import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
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

@SpringBootTest
@Transactional
class CommentRepositoryTest {

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

	private Comment savedComment(String content, LocalDateTime createdAt) {
		return commentRepository.save(
				Comment.builder()
						.content(content)
						.sharePage(sharePage)
						.writerId("user2")
						.createdAt(createdAt)
						.build()
		);
	}

	@Test
	@DisplayName("findBySharePageIdOrderByCreatedAtAsc - 등록순 정렬")
	void findBySharePageIdOrderByCreatedAtAsc_success() {
		// given
		LocalDateTime now = LocalDateTime.now();
		savedComment("두번째 댓글", now.plusMinutes(1));
		savedComment("첫번째 댓글", now);
		savedComment("세번째 댓글", now.plusMinutes(2));

		// when
		List<Comment> result = commentRepository.findBySharePageIdOrderByCreatedAtAsc(sharePage.getId());

		// then
		assertThat(result)
				.extracting(Comment::getContent)
				.containsExactly("첫번째 댓글", "두번째 댓글", "세번째 댓글");
	}

	@Test
	@DisplayName("findBySharePageIdOrderByCreatedAtAsc - 다른 게시글의 댓글은 제외")
	void findBySharePageIdOrderByCreatedAtAsc_excludesOtherSharePage() {
		// given
		Trip otherTrip = tripRepository.save(
				Trip.builder()
						.title("부산 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(2))
						.userId("user2")
						.build()
		);
		SharePage otherSharePage = sharePageRepository.save(
				SharePage.builder()
						.title("부산 여행기")
						.description("설명")
						.trip(otherTrip)
						.writerId("user2")
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
		commentRepository.save(
				Comment.builder()
						.content("다른 글 댓글")
						.sharePage(otherSharePage)
						.writerId("user3")
						.createdAt(LocalDateTime.now())
						.build()
		);
		savedComment("이 글 댓글", LocalDateTime.now());

		// when
		List<Comment> result = commentRepository.findBySharePageIdOrderByCreatedAtAsc(sharePage.getId());

		// then
		assertThat(result).hasSize(1)
				.extracting(Comment::getContent)
				.containsExactly("이 글 댓글");
	}
}

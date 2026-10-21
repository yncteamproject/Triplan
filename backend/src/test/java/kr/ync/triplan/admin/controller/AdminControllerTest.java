package kr.ync.triplan.admin.controller;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest extends BaseController {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private SharePageRepository sharePageRepository;

	@Autowired
	private CommentRepository commentRepository;

	private Member admin;
	private Member writer;
	private SharePage sharePage;
	private Comment comment;

	@BeforeEach
	void setUp() {
		admin = createMember("admin@test.com");
		admin.promoteToAdmin();
		memberRepository.save(admin);

		// 다른 사람이 쓴 게시글과 댓글
		writer = createMember("writer@test.com");
		Trip trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(2))
						.member(writer)
						.build()
		);
		sharePage = sharePageRepository.save(
				SharePage.builder()
						.title("제주도 2박 3일")
						.description("여행 후기")
						.trip(trip)
						.writer(writer)
						.writeDate(LocalDateTime.now())
						.viewCount(0)
						.build()
		);
		comment = commentRepository.save(
				Comment.builder()
						.content("좋은 정보 감사합니다")
						.sharePage(sharePage)
						.writer(writer)
						.createdAt(LocalDateTime.now())
						.build()
		);
	}

	@Test
	@DisplayName("A1. 관리자는 회원 · 게시글 · 댓글 수를 볼 수 있다")
	void getStats_admin() throws Exception {
		mockMvc.perform(get("/api/admin/stats").header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberCount").value(memberRepository.count()))
				.andExpect(jsonPath("$.sharePageCount").value(sharePageRepository.count()))
				.andExpect(jsonPath("$.commentCount").value(commentRepository.count()));
	}

	@Test
	@DisplayName("A1. 일반 회원은 403")
	void getStats_user() throws Exception {
		mockMvc.perform(get("/api/admin/stats").header("Authorization", bearer(writer)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
	}

	@Test
	@DisplayName("A1. 로그인하지 않으면 401")
	void getStats_unauthenticated() throws Exception {
		mockMvc.perform(get("/api/admin/stats"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("A2. 관리자는 게시글 목록을 최신순으로 본다 (작성자 이메일 포함)")
	void getSharePages_admin() throws Exception {
		mockMvc.perform(get("/api/admin/share-pages").header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(sharePage.getId()))
				.andExpect(jsonPath("$.content[0].writerEmail").value("writer@test.com"));
	}

	@Test
	@DisplayName("A2. page가 음수면 400")
	void getSharePages_invalidPage() throws Exception {
		mockMvc.perform(get("/api/admin/share-pages").param("page", "-1")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("A3. 관리자는 남의 게시글을 지울 수 있고, 달린 댓글도 같이 지워진다")
	void deleteSharePage_admin() throws Exception {
		mockMvc.perform(delete("/api/admin/share-pages/{id}", sharePage.getId())
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());

		assertThat(sharePageRepository.findById(sharePage.getId())).isEmpty();
		assertThat(commentRepository.findById(comment.getId())).isEmpty();
	}

	@Test
	@DisplayName("A3. 일반 회원은 관리자 API로 남의 게시글을 지울 수 없다 (403)")
	void deleteSharePage_user() throws Exception {
		Member other = createMember("other@test.com");

		mockMvc.perform(delete("/api/admin/share-pages/{id}", sharePage.getId())
						.header("Authorization", bearer(other)))
				.andExpect(status().isForbidden());

		assertThat(sharePageRepository.findById(sharePage.getId())).isPresent();
	}

	@Test
	@DisplayName("A3. 없는 게시글은 404")
	void deleteSharePage_notFound() throws Exception {
		mockMvc.perform(delete("/api/admin/share-pages/{id}", 999999L)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("A4. 관리자는 댓글 목록을 본다 (어느 글의 댓글인지 포함)")
	void getComments_admin() throws Exception {
		mockMvc.perform(get("/api/admin/comments").header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(comment.getId()))
				.andExpect(jsonPath("$.content[0].sharePageTitle").value("제주도 2박 3일"));
	}

	@Test
	@DisplayName("A5. 관리자는 남의 댓글을 지울 수 있다")
	void deleteComment_admin() throws Exception {
		mockMvc.perform(delete("/api/admin/comments/{id}", comment.getId())
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());

		assertThat(commentRepository.findById(comment.getId())).isEmpty();
		assertThat(sharePageRepository.findById(sharePage.getId())).isPresent();
	}

	@Test
	@DisplayName("A5. 없는 댓글은 404")
	void deleteComment_notFound() throws Exception {
		mockMvc.perform(delete("/api/admin/comments/{id}", 999999L)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("A6. 관리자는 회원 목록을 본다 (비밀번호는 없음)")
	void getMembers_admin() throws Exception {
		mockMvc.perform(get("/api/admin/members").header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].email").exists())
				.andExpect(jsonPath("$.content[0].password").doesNotExist());
	}

	@Test
	@DisplayName("A6. 일반 회원은 403")
	void getMembers_user() throws Exception {
		mockMvc.perform(get("/api/admin/members").header("Authorization", bearer(writer)))
				.andExpect(status().isForbidden());
	}
}
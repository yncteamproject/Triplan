package kr.ync.triplan.admin.service;

import kr.ync.triplan.admin.dto.response.AdminCommentResponse;
import kr.ync.triplan.admin.dto.response.AdminMemberResponse;
import kr.ync.triplan.admin.dto.response.AdminSharePageResponse;
import kr.ync.triplan.admin.dto.response.AdminStatsResponse;
import kr.ync.triplan.global.dto.PageResponse;
import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.global.exception.InvalidPageRequestException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.domain.Role;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.Comment;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.exception.CommentNotFoundException;
import kr.ync.triplan.share.exception.SharePageNotFoundException;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

	private static final int MAX_PAGE_SIZE = 50;

	private final MemberRepository memberRepository;
	private final SharePageRepository sharePageRepository;
	private final CommentRepository commentRepository;

	public AdminStatsResponse getStats(String email) {
		checkAdmin(email);
		return new AdminStatsResponse(
				memberRepository.count(),
				sharePageRepository.count(),
				commentRepository.count()
		);
	}

	public PageResponse<AdminSharePageResponse> getSharePages(String email, int page, int size) {
		checkAdmin(email);
		return PageResponse.from(
				sharePageRepository.findAll(latestFirst(page, size, "writeDate"))
						.map(AdminSharePageResponse::from));
	}

	// 게시글을 지우기 전에 달린 댓글을 먼저 지운다
	@Transactional
	public void deleteSharePage(String email, Long id) {
		checkAdmin(email);
		SharePage sharePage = sharePageRepository.findById(id)
				.orElseThrow(SharePageNotFoundException::new);
		commentRepository.deleteBySharePageId(id);
		sharePageRepository.delete(sharePage);
	}

	public PageResponse<AdminCommentResponse> getComments(String email, int page, int size) {
		checkAdmin(email);
		return PageResponse.from(
				commentRepository.findAll(latestFirst(page, size, "createdAt"))
						.map(AdminCommentResponse::from));
	}

	@Transactional
	public void deleteComment(String email, Long id) {
		checkAdmin(email);
		Comment comment = commentRepository.findById(id)
				.orElseThrow(CommentNotFoundException::new);
		commentRepository.delete(comment);
	}

	public PageResponse<AdminMemberResponse> getMembers(String email, int page, int size) {
		checkAdmin(email);
		return PageResponse.from(
				memberRepository.findAll(latestFirst(page, size, "createdAt"))
						.map(AdminMemberResponse::from));
	}

	private void checkAdmin(String email) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(ForbiddenException::new);
		if (member.getRole() != Role.ADMIN) {
			throw new ForbiddenException();
		}
	}

	// 최신순, 시각이 같으면 id 큰 것부터
	private Pageable latestFirst(int page, int size, String dateField) {
		if (page < 0 || size < 1) {
			throw new InvalidPageRequestException();
		}
		return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE),
				Sort.by(Sort.Order.desc(dateField), Sort.Order.desc("id")));
	}
}
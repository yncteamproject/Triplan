package kr.ync.triplan.service;

import kr.ync.triplan.domain.Comment;
import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.dto.request.CommentCreateRequest;
import kr.ync.triplan.dto.response.CommentResponse;
import kr.ync.triplan.exception.CommentNotFoundException;
import kr.ync.triplan.exception.MemberNotFoundException;
import kr.ync.triplan.exception.SharePageNotFoundException;
import kr.ync.triplan.repository.CommentRepository;
import kr.ync.triplan.repository.MemberRepository;
import kr.ync.triplan.repository.SharePageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final SharePageRepository sharePageRepository;
	private final MemberRepository memberRepository;

	@Override
	@Transactional
	public CommentResponse create(String email, Long sharePageId, CommentCreateRequest request) {
		Member writer = memberRepository.findByEmail(email)
				.orElseThrow(MemberNotFoundException::new);
		SharePage sharePage = sharePageRepository.findById(sharePageId)
				.orElseThrow(SharePageNotFoundException::new);

		Comment comment = Comment.builder()
				.content(request.content())
				.sharePage(sharePage)
				.writer(writer)
				.createdAt(LocalDateTime.now())
				.build();

		return CommentResponse.from(commentRepository.save(comment));
	}

	@Override
	public List<CommentResponse> getList(Long sharePageId) {
		return commentRepository.findBySharePageIdOrderByCreatedAtAsc(sharePageId).stream()
				.map(CommentResponse::from)
				.toList();
	}

	@Override
	@Transactional
	public void delete(String email, Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(CommentNotFoundException::new);
		comment.validateWriter(email);
		commentRepository.delete(comment);
	}
}

package kr.ync.triplan.share.service;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.exception.MemberNotFoundException;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.exception.SharePageNotFoundException;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharePageServiceImpl implements SharePageService {

	private final SharePageRepository sharePageRepository;
	private final TripRepository tripRepository;
	private final MemberRepository memberRepository;
	private final CommentRepository commentRepository;

	// 게시글 작성 (본인 여행만 공유 가능)
	@Override
	@Transactional
	public SharePageResponse create(String email, SharePageCreateRequest request) {
		Member writer = memberRepository.findByEmail(email)
				.orElseThrow(MemberNotFoundException::new);
		Trip trip = tripRepository.findById(request.tripId())
				.orElseThrow(TripNotFoundException::new);
		trip.validateOwner(email);

		SharePage sharePage = SharePage.builder()
				.title(request.title())
				.description(request.description())
				.trip(trip)
				.writer(writer)
				.writeDate(LocalDateTime.now())
				.build();

		return SharePageResponse.from(sharePageRepository.save(sharePage));
	}

	// 게시글 리스트
	@Override
	public List<SharePageListResponse> getList() {
		return sharePageRepository.findAllByOrderByWriteDateDesc().stream()
				.map(SharePageListResponse::from)
				.toList();
	}

	// 게시글 상세보기
	@Override
	@Transactional
	public SharePageResponse getDetail(Long id) {
		SharePage sharePage = findById(id);
		sharePage.setViewCount(sharePage.getViewCount() + 1);
		return SharePageResponse.from(sharePage);
	}

	// 게시글 수정 (작성자만)
	@Override
	@Transactional
	public SharePageResponse update(String email, Long id, SharePageUpdateRequest request) {
		SharePage sharePage = findById(id);
		sharePage.validateWriter(email);
		sharePage.setTitle(request.title());
		sharePage.setDescription(request.description());
		sharePage.setUpdateDate(LocalDateTime.now());
		return SharePageResponse.from(sharePage);
	}

	// 게시글 삭제 (작성자만)
	@Override
	@Transactional
	public void delete(String email, Long id) {
		SharePage sharePage = findById(id);
		sharePage.validateWriter(email);
		commentRepository.deleteBySharePageId(id);
		sharePageRepository.delete(sharePage);
	}

	private SharePage findById(Long id) {
		return sharePageRepository.findById(id)
				.orElseThrow(SharePageNotFoundException::new);
	}
}

package kr.ync.triplan.service;

import kr.ync.triplan.domain.SharePage;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.SharePageCreateRequest;
import kr.ync.triplan.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.dto.response.SharePageListResponse;
import kr.ync.triplan.dto.response.SharePageResponse;
import kr.ync.triplan.exception.SharePageNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.SharePageRepository;
import kr.ync.triplan.repository.TripRepository;
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

	// 게시글 작성
	@Override
	@Transactional
	public SharePageResponse create(SharePageCreateRequest request) {
		Trip trip = tripRepository.findById(request.tripId())
				.orElseThrow(TripNotFoundException::new);

		SharePage sharePage = SharePage.builder()
				.title(request.title())
				.description(request.description())
				.trip(trip)
				.writerId(request.writerId())
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


	// 게시글 수정
	@Override
	@Transactional
	public SharePageResponse update(Long id, SharePageUpdateRequest request) {
		SharePage sharePage = findById(id);
		sharePage.setTitle(request.title());
		sharePage.setDescription(request.description());
		sharePage.setUpdateDate(LocalDateTime.now());
		return SharePageResponse.from(sharePage);
	}


	// 게시글 삭제
	@Override
	@Transactional
	public void delete(Long id) {
		sharePageRepository.delete(findById(id));
	}


	// 게시글
	private SharePage findById(Long id) {
		return sharePageRepository.findById(id)
				.orElseThrow(SharePageNotFoundException::new);
	}
}

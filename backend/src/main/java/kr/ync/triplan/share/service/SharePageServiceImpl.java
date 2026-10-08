package kr.ync.triplan.share.service;

import kr.ync.triplan.global.dto.PageResponse;
import kr.ync.triplan.global.exception.InvalidPageRequestException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.exception.MemberNotFoundException;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.domain.SharePage;
import kr.ync.triplan.share.dto.request.SharePageCreateRequest;
import kr.ync.triplan.share.dto.request.SharePageUpdateRequest;
import kr.ync.triplan.share.dto.request.TripCopyRequest;
import kr.ync.triplan.share.dto.response.SharePageListResponse;
import kr.ync.triplan.share.dto.response.SharePageResponse;
import kr.ync.triplan.share.dto.response.SharedTripResponse;
import kr.ync.triplan.share.exception.SharePageNotFoundException;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.response.TripResponse;
import kr.ync.triplan.trip.dto.response.TripSummary;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import kr.ync.triplan.trip.service.TripSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharePageServiceImpl implements SharePageService {

	private final SharePageRepository sharePageRepository;
	private final TripRepository tripRepository;
	private final MemberRepository memberRepository;
	private final CommentRepository commentRepository;
	private final StopRepository stopRepository;
	private final TransportSegmentRepository transportSegmentRepository;
	private final LodgingRepository lodgingRepository;
	private final TripSummaryService tripSummaryService;

	// 한 번에 가져갈 수 있는 게시글 수 상한 (size를 크게 보내 전체를 가져가는 것 방지)
	private static final int MAX_PAGE_SIZE = 50;

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
				.allowCopy(request.allowCopy() == null || request.allowCopy())
				.build();

		return SharePageResponse.from(sharePageRepository.save(sharePage));
	}

	// 게시글 리스트 (최신 작성순, 페이지 단위)
	@Override
	public PageResponse<SharePageListResponse> getList(int page, int size) {
		if (page < 0 || size < 1) {
			throw new InvalidPageRequestException();
		}
		// 작성 시각이 같으면 나중에 만든 글이 먼저 오도록 id로 한 번 더 정렬 (페이지 사이에 글이 겹치거나 빠지지 않게)
		Pageable pageable = PageRequest.of(
				page, Math.min(size, MAX_PAGE_SIZE),
				Sort.by(Sort.Order.desc("writeDate"), Sort.Order.desc("id")));
		Page<SharePage> sharePages = sharePageRepository.findAll(pageable);

		// 이 페이지에 나온 여행들의 요약(지역 · 방문지 수 · 총 경비)을 한 번에 구한다
		List<Long> tripIds = sharePages.getContent().stream()
				.map(sharePage -> sharePage.getTrip().getId())
				.distinct()
				.toList();
		Map<Long, TripSummary> summaries = tripSummaryService.summarize(tripIds);

		return PageResponse.from(sharePages.map(sharePage ->
				SharePageListResponse.of(sharePage, summaries.get(sharePage.getTrip().getId()))));
	}

	// 게시글 상세보기
	@Override
	@Transactional
	public SharePageResponse getDetail(Long id) {
		SharePage sharePage = findById(id);
		sharePage.setViewCount(sharePage.getViewCount() + 1);
		return SharePageResponse.from(sharePage);
	}

	// 공유된 여행 상세 (누구나 조회, 예약번호 제외)
	@Override
	public SharedTripResponse getSharedTrip(Long id) {
		Trip trip = findById(id).getTrip();
		return SharedTripResponse.of(
				trip,
				stopRepository.findByTripIdOrderByStopOrderAsc(trip.getId()),
				transportSegmentRepository.findByTripId(trip.getId()),
				lodgingRepository.findByTripId(trip.getId())
		);
	}

	// 게시글 수정 (작성자만)
	@Override
	@Transactional
	public SharePageResponse update(String email, Long id, SharePageUpdateRequest request) {
		SharePage sharePage = findById(id);
		sharePage.validateWriter(email);
		sharePage.setTitle(request.title());
		sharePage.setDescription(request.description());
		if (request.allowCopy() != null) {
			sharePage.setAllowCopy(request.allowCopy());
		}
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

	// 공유된 여행을 내 여행으로 복사 (복사 허용된 게시글만, 예약번호는 가져오지 않음)
	@Override
	@Transactional
	public TripResponse copyTrip(String email, Long id, TripCopyRequest request) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(MemberNotFoundException::new);
		SharePage sharePage = findById(id);
		sharePage.validateCopyable(email);

		Trip source = sharePage.getTrip();
		// 시작일을 지정하면 원본 시작일과의 차이만큼 모든 날짜를 옮긴다
		long days = (request == null || request.startDate() == null)
				? 0
				: ChronoUnit.DAYS.between(source.getStartDate(), request.startDate());

		Trip copy = tripRepository.save(
				Trip.builder()
						.title(source.getTitle())
						.startDate(source.getStartDate().plusDays(days))
						.endDate(source.getEndDate().plusDays(days))
						.member(member)
						.build()
		);

		// 원본 방문지 id → 복사한 방문지 (이동 구간을 새 방문지에 다시 연결하기 위해)
		Map<Long, Stop> copiedStops = new HashMap<>();
		for (Stop stop : stopRepository.findByTripIdOrderByStopOrderAsc(source.getId())) {
			copiedStops.put(stop.getId(), stopRepository.save(
					Stop.builder()
							.trip(copy)
							.name(stop.getName())
							.date(stop.getDate().plusDays(days))
							.time(stop.getTime())
							.memo(stop.getMemo())
							.imageUrl(stop.getImageUrl())
							.stopOrder(stop.getStopOrder())
							.latitude(stop.getLatitude())
							.longitude(stop.getLongitude())
							.address(stop.getAddress())
							.build()
			));
		}

		for (TransportSegment segment : transportSegmentRepository.findByTripId(source.getId())) {
			transportSegmentRepository.save(
					TransportSegment.builder()
							.trip(copy)
							.fromStop(copiedStops.get(segment.getFromStop().getId()))
							.toStop(copiedStops.get(segment.getToStop().getId()))
							.mode(segment.getMode())
							.departTime(segment.getDepartTime().plusDays(days))
							.arriveTime(segment.getArriveTime().plusDays(days))
							.cost(segment.getCost())
							.build()
			);
		}

		for (Lodging lodging : lodgingRepository.findByTripId(source.getId())) {
			lodgingRepository.save(
					Lodging.builder()
							.trip(copy)
							.name(lodging.getName())
							.checkIn(lodging.getCheckIn().plusDays(days))
							.checkOut(lodging.getCheckOut().plusDays(days))
							.cost(lodging.getCost())
							.build()
			);
		}

		sharePage.setCopyCount(sharePage.getCopyCount() + 1);
		return TripResponse.from(copy);
	}

	private SharePage findById(Long id) {
		return sharePageRepository.findById(id)
				.orElseThrow(SharePageNotFoundException::new);
	}
}

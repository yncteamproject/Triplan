package kr.ync.triplan.trip.service;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.member.exception.MemberNotFoundException;
import kr.ync.triplan.member.repository.MemberRepository;
import kr.ync.triplan.share.repository.CommentRepository;
import kr.ync.triplan.share.repository.SharePageRepository;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.TripCreateRequest;
import kr.ync.triplan.trip.dto.request.TripUpdateRequest;
import kr.ync.triplan.trip.dto.response.TripEstimateResponse;
import kr.ync.triplan.trip.dto.response.TripResponse;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripServiceImpl implements TripService {

	private final TripRepository tripRepository;
	private final MemberRepository memberRepository;
	private final TransportSegmentRepository transportSegmentRepository;
	private final LodgingRepository lodgingRepository;
	private final StopRepository stopRepository;
	private final SharePageRepository sharePageRepository;
	private final CommentRepository commentRepository;

	@Override
	@Transactional
	public TripResponse create(String email, TripCreateRequest request) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(MemberNotFoundException::new);

		Trip trip = Trip.builder()
				.title(request.title())
				.startDate(request.startDate())
				.endDate(request.endDate())
				.member(member)
				.build();

		return TripResponse.from(tripRepository.save(trip));
	}

	@Override
	public List<TripResponse> getList(String email) {
		return tripRepository.findByMemberEmail(email).stream()
				.map(TripResponse::from)
				.toList();
	}

	@Override
	public TripResponse getDetail(String email, Long id) {
		return TripResponse.from(findOwnedTrip(email, id));
	}

	@Override
	@Transactional
	public TripResponse update(String email, Long id, TripUpdateRequest request) {
		Trip trip = findOwnedTrip(email, id);
		trip.setTitle(request.title());
		trip.setStartDate(request.startDate());
		trip.setEndDate(request.endDate());
		return TripResponse.from(trip);
	}

	@Override
	@Transactional
	public void delete(String email, Long id) {
		Trip trip = findOwnedTrip(email, id);

		// 외래키 때문에 하위 데이터부터 삭제 (이동 구간이 방문지를 참조하므로 방문지보다 먼저)
		commentRepository.deleteBySharePageTripId(id);
		sharePageRepository.deleteByTripId(id);
		transportSegmentRepository.deleteByTripId(id);
		lodgingRepository.deleteByTripId(id);
		stopRepository.deleteByTripId(id);
		tripRepository.delete(trip);
	}

	@Override
	public TripEstimateResponse getEstimate(String email, Long id) {
		findOwnedTrip(email, id);

		int transportCost = transportSegmentRepository.findByTripId(id).stream()
				.mapToInt(segment -> segment.getCost() == null ? 0 : segment.getCost())
				.sum();

		int lodgingCost = lodgingRepository.findByTripId(id).stream()
				.mapToInt(lodging -> lodging.getCost() == null ? 0 : lodging.getCost())
				.sum();

		return TripEstimateResponse.of(id, transportCost, lodgingCost);
	}

	private Trip findOwnedTrip(String email, Long id) {
		Trip trip = tripRepository.findById(id)
				.orElseThrow(TripNotFoundException::new);
		trip.validateOwner(email);
		return trip;
	}
}

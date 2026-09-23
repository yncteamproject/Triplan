package kr.ync.triplan.service;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripEstimateResponse;
import kr.ync.triplan.dto.response.TripResponse;
import kr.ync.triplan.exception.MemberNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.LodgingRepository;
import kr.ync.triplan.repository.MemberRepository;
import kr.ync.triplan.repository.TransportSegmentRepository;
import kr.ync.triplan.repository.TripRepository;
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
		tripRepository.delete(findOwnedTrip(email, id));
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

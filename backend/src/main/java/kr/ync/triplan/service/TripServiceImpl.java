package kr.ync.triplan.service;

import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripEstimateResponse;
import kr.ync.triplan.dto.response.TripResponse;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.LodgingRepository;
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
	private final TransportSegmentRepository transportSegmentRepository;
	private final LodgingRepository lodgingRepository;

	@Override
	@Transactional
	public TripResponse create(TripCreateRequest request) {
		Trip trip = Trip.builder()
				.title(request.title())
				.startDate(request.startDate())
				.endDate(request.endDate())
				.userId(request.userId())
				.build();

		return TripResponse.from(tripRepository.save(trip));
	}

	@Override
	public List<TripResponse> getList(String userId) {
		return tripRepository.findByUserId(userId).stream()
				.map(TripResponse::from)
				.toList();
	}

	@Override
	public TripResponse getDetail(Long id) {
		return TripResponse.from(findById(id));
	}

	@Override
	@Transactional
	public TripResponse update(Long id, TripUpdateRequest request) {
		Trip trip = findById(id);
		trip.setTitle(request.title());
		trip.setStartDate(request.startDate());
		trip.setEndDate(request.endDate());
		return TripResponse.from(trip);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		tripRepository.delete(findById(id));
	}

	@Override
	public TripEstimateResponse getEstimate(Long id) {
		findById(id);

		int transportCost = transportSegmentRepository.findByTripId(id).stream()
				.mapToInt(segment -> segment.getCost() == null ? 0 : segment.getCost())
				.sum();

		int lodgingCost = lodgingRepository.findByTripId(id).stream()
				.mapToInt(lodging -> lodging.getCost() == null ? 0 : lodging.getCost())
				.sum();

		return TripEstimateResponse.of(id, transportCost, lodgingCost);
	}

	private Trip findById(Long id) {
		return tripRepository.findById(id)
				.orElseThrow(TripNotFoundException::new);
	}
}

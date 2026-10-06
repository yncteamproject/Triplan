package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.StopCreateRequest;
import kr.ync.triplan.trip.dto.request.StopUpdateRequest;
import kr.ync.triplan.trip.dto.response.StopResponse;
import kr.ync.triplan.trip.exception.StopNotFoundException;
import kr.ync.triplan.trip.exception.TripNotFoundException;
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
public class StopServiceImpl implements StopService {

	private final StopRepository stopRepository;
	private final TripRepository tripRepository;
	private final TransportSegmentRepository transportSegmentRepository;

	@Override
	@Transactional
	public StopResponse create(String email, Long tripId, StopCreateRequest request) {
		Trip trip = findOwnedTrip(email, tripId);

		Stop stop = Stop.builder()
				.trip(trip)
				.name(request.name())
				.date(request.date())
				.time(request.time())
				.memo(request.memo())
				.imageUrl(request.imageUrl())
				.stopOrder(request.stopOrder())
				.latitude(request.latitude())
				.longitude(request.longitude())
				.address(request.address())
				.build();

		return StopResponse.from(stopRepository.save(stop));
	}

	@Override
	public List<StopResponse> getList(String email, Long tripId) {
		findOwnedTrip(email, tripId);
		return stopRepository.findByTripIdOrderByStopOrderAsc(tripId).stream()
				.map(StopResponse::from)
				.toList();
	}

	@Override
	public StopResponse getDetail(String email, Long id) {
		return StopResponse.from(findOwnedStop(email, id));
	}

	@Override
	@Transactional
	public StopResponse update(String email, Long id, StopUpdateRequest request) {
		Stop stop = findOwnedStop(email, id);
		stop.setName(request.name());
		stop.setDate(request.date());
		stop.setTime(request.time());
		stop.setMemo(request.memo());
		stop.setImageUrl(request.imageUrl());
		stop.setStopOrder(request.stopOrder());
		stop.setLatitude(request.latitude());
		stop.setLongitude(request.longitude());
		stop.setAddress(request.address());
		return StopResponse.from(stop);
	}

	@Override
	@Transactional
	public void delete(String email, Long id) {
		Stop stop = findOwnedStop(email, id);
		transportSegmentRepository.deleteByFromStopIdOrToStopId(id, id);
		stopRepository.delete(stop);
	}

	private Trip findOwnedTrip(String email, Long tripId) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);
		trip.validateOwner(email);
		return trip;
	}

	private Stop findOwnedStop(String email, Long id) {
		Stop stop = stopRepository.findById(id)
				.orElseThrow(StopNotFoundException::new);
		stop.getTrip().validateOwner(email);
		return stop;
	}
}

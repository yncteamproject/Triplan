package kr.ync.triplan.service;

import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.StopCreateRequest;
import kr.ync.triplan.dto.request.StopUpdateRequest;
import kr.ync.triplan.dto.response.StopResponse;
import kr.ync.triplan.exception.StopNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TripRepository;
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

	@Override
	@Transactional
	public StopResponse create(Long tripId, StopCreateRequest request) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);

		Stop stop = Stop.builder()
				.trip(trip)
				.name(request.name())
				.date(request.date())
				.time(request.time())
				.memo(request.memo())
				.imageUrl(request.imageUrl())
				.stopOrder(request.stopOrder())
				.build();

		return StopResponse.from(stopRepository.save(stop));
	}

	@Override
	public List<StopResponse> getList(Long tripId) {
		return stopRepository.findByTripIdOrderByStopOrderAsc(tripId).stream()
				.map(StopResponse::from)
				.toList();
	}

	@Override
	public StopResponse getDetail(Long id) {
		return StopResponse.from(findById(id));
	}

	@Override
	@Transactional
	public StopResponse update(Long id, StopUpdateRequest request) {
		Stop stop = findById(id);
		stop.setName(request.name());
		stop.setDate(request.date());
		stop.setTime(request.time());
		stop.setMemo(request.memo());
		stop.setImageUrl(request.imageUrl());
		stop.setStopOrder(request.stopOrder());
		return StopResponse.from(stop);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		stopRepository.delete(findById(id));
	}

	private Stop findById(Long id) {
		return stopRepository.findById(id)
				.orElseThrow(StopNotFoundException::new);
	}
}

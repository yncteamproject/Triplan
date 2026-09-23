package kr.ync.triplan.service;

import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.domain.TransportSegment;
import kr.ync.triplan.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.dto.response.TransportSegmentResponse;
import kr.ync.triplan.exception.StopNotFoundException;
import kr.ync.triplan.exception.TransportSegmentNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TransportSegmentRepository;
import kr.ync.triplan.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransportSegmentServiceImpl implements TransportSegmentService {

	private final TransportSegmentRepository transportSegmentRepository;
	private final TripRepository tripRepository;
	private final StopRepository stopRepository;

	@Override
	@Transactional
	public TransportSegmentResponse create(Long tripId, TransportSegmentCreateRequest request) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);
		Stop fromStop = findStopById(request.fromStopId());
		Stop toStop = findStopById(request.toStopId());

		TransportSegment segment = TransportSegment.builder()
				.trip(trip)
				.fromStop(fromStop)
				.toStop(toStop)
				.mode(request.mode())
				.departTime(request.departTime())
				.arriveTime(request.arriveTime())
				.cost(request.cost())
				.reservationNo(request.reservationNo())
				.build();

		return TransportSegmentResponse.from(transportSegmentRepository.save(segment));
	}

	@Override
	public List<TransportSegmentResponse> getList(Long tripId) {
		return transportSegmentRepository.findByTripId(tripId).stream()
				.map(TransportSegmentResponse::from)
				.toList();
	}

	@Override
	public TransportSegmentResponse getDetail(Long id) {
		return TransportSegmentResponse.from(findById(id));
	}

	@Override
	@Transactional
	public TransportSegmentResponse update(Long id, TransportSegmentUpdateRequest request) {
		TransportSegment segment = findById(id);
		Stop fromStop = findStopById(request.fromStopId());
		Stop toStop = findStopById(request.toStopId());

		segment.setFromStop(fromStop);
		segment.setToStop(toStop);
		segment.setMode(request.mode());
		segment.setDepartTime(request.departTime());
		segment.setArriveTime(request.arriveTime());
		segment.setCost(request.cost());
		segment.setReservationNo(request.reservationNo());

		return TransportSegmentResponse.from(segment);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		transportSegmentRepository.delete(findById(id));
	}

	private TransportSegment findById(Long id) {
		return transportSegmentRepository.findById(id)
				.orElseThrow(TransportSegmentNotFoundException::new);
	}

	private Stop findStopById(Long id) {
		return stopRepository.findById(id)
				.orElseThrow(StopNotFoundException::new);
	}
}

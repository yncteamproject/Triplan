package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.trip.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.trip.dto.response.TransportSegmentResponse;
import kr.ync.triplan.trip.exception.StopNotFoundException;
import kr.ync.triplan.trip.exception.TransportSegmentNotFoundException;
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
public class TransportSegmentServiceImpl implements TransportSegmentService {

	private final TransportSegmentRepository transportSegmentRepository;
	private final TripRepository tripRepository;
	private final StopRepository stopRepository;

	@Override
	@Transactional
	public TransportSegmentResponse create(String email, Long tripId, TransportSegmentCreateRequest request) {
		Trip trip = findOwnedTrip(email, tripId);
		Stop fromStop = findOwnedStop(email, request.fromStopId());
		Stop toStop = findOwnedStop(email, request.toStopId());

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
	public List<TransportSegmentResponse> getList(String email, Long tripId) {
		findOwnedTrip(email, tripId);
		return transportSegmentRepository.findByTripId(tripId).stream()
				.map(TransportSegmentResponse::from)
				.toList();
	}

	@Override
	public TransportSegmentResponse getDetail(String email, Long id) {
		return TransportSegmentResponse.from(findOwnedSegment(email, id));
	}

	@Override
	@Transactional
	public TransportSegmentResponse update(String email, Long id, TransportSegmentUpdateRequest request) {
		TransportSegment segment = findOwnedSegment(email, id);
		Stop fromStop = findOwnedStop(email, request.fromStopId());
		Stop toStop = findOwnedStop(email, request.toStopId());

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
	public void delete(String email, Long id) {
		transportSegmentRepository.delete(findOwnedSegment(email, id));
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

	private TransportSegment findOwnedSegment(String email, Long id) {
		TransportSegment segment = transportSegmentRepository.findById(id)
				.orElseThrow(TransportSegmentNotFoundException::new);
		segment.getTrip().validateOwner(email);
		return segment;
	}
}

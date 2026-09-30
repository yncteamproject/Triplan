package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.request.LodgingCreateRequest;
import kr.ync.triplan.trip.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.trip.dto.response.LodgingResponse;
import kr.ync.triplan.trip.exception.LodgingNotFoundException;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LodgingServiceImpl implements LodgingService {

	private final LodgingRepository lodgingRepository;
	private final TripRepository tripRepository;

	@Override
	@Transactional
	public LodgingResponse create(String email, Long tripId, LodgingCreateRequest request) {
		Trip trip = findOwnedTrip(email, tripId);

		Lodging lodging = Lodging.builder()
				.trip(trip)
				.name(request.name())
				.checkIn(request.checkIn())
				.checkOut(request.checkOut())
				.cost(request.cost())
				.reservationNo(request.reservationNo())
				.build();

		return LodgingResponse.from(lodgingRepository.save(lodging));
	}

	@Override
	public List<LodgingResponse> getList(String email, Long tripId) {
		findOwnedTrip(email, tripId);
		return lodgingRepository.findByTripId(tripId).stream()
				.map(LodgingResponse::from)
				.toList();
	}

	@Override
	public LodgingResponse getDetail(String email, Long id) {
		return LodgingResponse.from(findOwnedLodging(email, id));
	}

	@Override
	@Transactional
	public LodgingResponse update(String email, Long id, LodgingUpdateRequest request) {
		Lodging lodging = findOwnedLodging(email, id);
		lodging.setName(request.name());
		lodging.setCheckIn(request.checkIn());
		lodging.setCheckOut(request.checkOut());
		lodging.setCost(request.cost());
		lodging.setReservationNo(request.reservationNo());
		return LodgingResponse.from(lodging);
	}

	@Override
	@Transactional
	public void delete(String email, Long id) {
		lodgingRepository.delete(findOwnedLodging(email, id));
	}

	private Trip findOwnedTrip(String email, Long tripId) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);
		trip.validateOwner(email);
		return trip;
	}

	private Lodging findOwnedLodging(String email, Long id) {
		Lodging lodging = lodgingRepository.findById(id)
				.orElseThrow(LodgingNotFoundException::new);
		lodging.getTrip().validateOwner(email);
		return lodging;
	}
}

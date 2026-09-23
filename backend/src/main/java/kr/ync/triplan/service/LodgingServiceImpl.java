package kr.ync.triplan.service;

import kr.ync.triplan.domain.Lodging;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.LodgingCreateRequest;
import kr.ync.triplan.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.dto.response.LodgingResponse;
import kr.ync.triplan.exception.LodgingNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.LodgingRepository;
import kr.ync.triplan.repository.TripRepository;
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
	public LodgingResponse create(Long tripId, LodgingCreateRequest request) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);

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
	public List<LodgingResponse> getList(Long tripId) {
		return lodgingRepository.findByTripId(tripId).stream()
				.map(LodgingResponse::from)
				.toList();
	}

	@Override
	public LodgingResponse getDetail(Long id) {
		return LodgingResponse.from(findById(id));
	}

	@Override
	@Transactional
	public LodgingResponse update(Long id, LodgingUpdateRequest request) {
		Lodging lodging = findById(id);
		lodging.setName(request.name());
		lodging.setCheckIn(request.checkIn());
		lodging.setCheckOut(request.checkOut());
		lodging.setCost(request.cost());
		lodging.setReservationNo(request.reservationNo());
		return LodgingResponse.from(lodging);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		lodgingRepository.delete(findById(id));
	}

	private Lodging findById(Long id) {
		return lodgingRepository.findById(id)
				.orElseThrow(LodgingNotFoundException::new);
	}
}

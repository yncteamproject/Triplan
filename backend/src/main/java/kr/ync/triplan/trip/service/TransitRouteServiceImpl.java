package kr.ync.triplan.trip.service;

import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.trip.client.OdsayClient;
import kr.ync.triplan.trip.client.OdsayResponseParser;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.Trip;
import kr.ync.triplan.trip.dto.response.TransitRouteResponse;
import kr.ync.triplan.trip.exception.InvalidTransitRouteRequestException;
import kr.ync.triplan.trip.exception.StopNotFoundException;
import kr.ync.triplan.trip.exception.TripNotFoundException;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransitRouteServiceImpl implements TransitRouteService {

	private final TripRepository tripRepository;
	private final StopRepository stopRepository;
	private final OdsayClient odsayClient;
	private final OdsayResponseParser odsayResponseParser;
	private final TransitRouteCache transitRouteCache;

	// 두 방문지 사이 대중교통 경로 (여행 주인만, 이 여행의 방문지만)
	@Override
	public TransitRouteResponse getRoutes(String email, Long tripId, Long fromStopId, Long toStopId) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(TripNotFoundException::new);
		trip.validateOwner(email);

		if (fromStopId.equals(toStopId)) {
			throw new InvalidTransitRouteRequestException("출발지와 도착지가 같습니다.");
		}
		Stop from = findStopInTrip(fromStopId, tripId);
		Stop to = findStopInTrip(toStopId, tripId);
		if (from.getLatitude() == null || to.getLatitude() == null) {
			throw new InvalidTransitRouteRequestException("방문지의 위치(위도 · 경도)를 먼저 입력해주세요.");
		}

		// 오디세이는 경도(x), 위도(y) 순서
		double startX = from.getLongitude();
		double startY = from.getLatitude();
		double endX = to.getLongitude();
		double endY = to.getLatitude();
		return new TransitRouteResponse(transitRouteCache.get(startX, startY, endX, endY,
				() -> odsayResponseParser.parse(odsayClient.searchPubTransPath(startX, startY, endX, endY))));
	}

	private Stop findStopInTrip(Long stopId, Long tripId) {
		Stop stop = stopRepository.findById(stopId)
				.orElseThrow(StopNotFoundException::new);
		if (!stop.getTrip().getId().equals(tripId)) {
			throw new ForbiddenException();
		}
		return stop;
	}
}

package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.dto.response.TransitRouteResponse;

public interface TransitRouteService {

	TransitRouteResponse getRoutes(String email, Long tripId, Long fromStopId, Long toStopId);
}

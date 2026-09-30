package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.dto.request.LodgingCreateRequest;
import kr.ync.triplan.trip.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.trip.dto.response.LodgingResponse;

import java.util.List;

public interface LodgingService {

	LodgingResponse create(String email, Long tripId, LodgingCreateRequest request);

	List<LodgingResponse> getList(String email, Long tripId);

	LodgingResponse getDetail(String email, Long id);

	LodgingResponse update(String email, Long id, LodgingUpdateRequest request);

	void delete(String email, Long id);
}

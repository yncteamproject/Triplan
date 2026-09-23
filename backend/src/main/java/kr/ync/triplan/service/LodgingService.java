package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.LodgingCreateRequest;
import kr.ync.triplan.dto.request.LodgingUpdateRequest;
import kr.ync.triplan.dto.response.LodgingResponse;

import java.util.List;

public interface LodgingService {

	LodgingResponse create(Long tripId, LodgingCreateRequest request);

	List<LodgingResponse> getList(Long tripId);

	LodgingResponse getDetail(Long id);

	LodgingResponse update(Long id, LodgingUpdateRequest request);

	void delete(Long id);
}

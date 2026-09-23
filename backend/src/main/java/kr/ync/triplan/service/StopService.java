package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.StopCreateRequest;
import kr.ync.triplan.dto.request.StopUpdateRequest;
import kr.ync.triplan.dto.response.StopResponse;

import java.util.List;

public interface StopService {

	StopResponse create(Long tripId, StopCreateRequest request);

	List<StopResponse> getList(Long tripId);

	StopResponse getDetail(Long id);

	StopResponse update(Long id, StopUpdateRequest request);

	void delete(Long id);
}

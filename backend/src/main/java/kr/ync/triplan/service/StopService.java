package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.StopCreateRequest;
import kr.ync.triplan.dto.request.StopUpdateRequest;
import kr.ync.triplan.dto.response.StopResponse;

import java.util.List;

public interface StopService {

	StopResponse create(String email, Long tripId, StopCreateRequest request);

	List<StopResponse> getList(String email, Long tripId);

	StopResponse getDetail(String email, Long id);

	StopResponse update(String email, Long id, StopUpdateRequest request);

	void delete(String email, Long id);
}

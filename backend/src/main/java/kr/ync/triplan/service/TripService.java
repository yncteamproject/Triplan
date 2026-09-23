package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripEstimateResponse;
import kr.ync.triplan.dto.response.TripResponse;

import java.util.List;

public interface TripService {

	TripResponse create(TripCreateRequest request);

	List<TripResponse> getList(String userId);

	TripResponse getDetail(Long id);

	TripResponse update(Long id, TripUpdateRequest request);

	void delete(Long id);

	TripEstimateResponse getEstimate(Long id);
}

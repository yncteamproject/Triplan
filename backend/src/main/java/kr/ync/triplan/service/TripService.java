package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.TripCreateRequest;
import kr.ync.triplan.dto.request.TripUpdateRequest;
import kr.ync.triplan.dto.response.TripEstimateResponse;
import kr.ync.triplan.dto.response.TripResponse;

import java.util.List;

public interface TripService {

	TripResponse create(String email, TripCreateRequest request);

	List<TripResponse> getList(String email);

	TripResponse getDetail(String email, Long id);

	TripResponse update(String email, Long id, TripUpdateRequest request);

	void delete(String email, Long id);

	TripEstimateResponse getEstimate(String email, Long id);
}

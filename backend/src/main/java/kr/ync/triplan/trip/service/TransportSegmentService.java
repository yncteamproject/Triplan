package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.trip.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.trip.dto.response.TransportSegmentResponse;

import java.util.List;

public interface TransportSegmentService {

	TransportSegmentResponse create(String email, Long tripId, TransportSegmentCreateRequest request);

	List<TransportSegmentResponse> getList(String email, Long tripId);

	TransportSegmentResponse getDetail(String email, Long id);

	TransportSegmentResponse update(String email, Long id, TransportSegmentUpdateRequest request);

	void delete(String email, Long id);
}

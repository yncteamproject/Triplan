package kr.ync.triplan.service;

import kr.ync.triplan.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.dto.response.TransportSegmentResponse;

import java.util.List;

public interface TransportSegmentService {

	TransportSegmentResponse create(Long tripId, TransportSegmentCreateRequest request);

	List<TransportSegmentResponse> getList(Long tripId);

	TransportSegmentResponse getDetail(Long id);

	TransportSegmentResponse update(Long id, TransportSegmentUpdateRequest request);

	void delete(Long id);
}

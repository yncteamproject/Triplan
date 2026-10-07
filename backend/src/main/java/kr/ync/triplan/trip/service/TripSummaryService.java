package kr.ync.triplan.trip.service;

import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.dto.response.TripSummary;
import kr.ync.triplan.trip.repository.LodgingRepository;
import kr.ync.triplan.trip.repository.StopRepository;
import kr.ync.triplan.trip.repository.TransportSegmentRepository;
import kr.ync.triplan.trip.repository.TripIdAmount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 여러 여행의 요약(지역 · 방문지 수 · 총 경비)을 한꺼번에 구한다
// 여행 수와 상관없이 쿼리는 4번만 나간다 (목록에서 글마다 따로 조회하지 않도록)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripSummaryService {

	private final StopRepository stopRepository;
	private final TransportSegmentRepository transportSegmentRepository;
	private final LodgingRepository lodgingRepository;

	// 여행 id → 요약. 방문지 · 비용이 없는 여행도 빠지지 않고 들어 있다 (지역 null, 0, 0)
	public Map<Long, TripSummary> summarize(Collection<Long> tripIds) {
		Map<Long, TripSummary> summaries = new HashMap<>();
		if (tripIds.isEmpty()) {
			return summaries;
		}

		Map<Long, Long> stopCounts = toMap(stopRepository.countByTripIds(tripIds));
		Map<Long, Long> transportCosts = toMap(transportSegmentRepository.sumCostByTripIds(tripIds));
		Map<Long, Long> lodgingCosts = toMap(lodgingRepository.sumCostByTripIds(tripIds));
		Map<Long, String> regions = findRegions(tripIds);

		for (Long tripId : tripIds) {
			long totalCost = transportCosts.getOrDefault(tripId, 0L) + lodgingCosts.getOrDefault(tripId, 0L);
			summaries.put(tripId, new TripSummary(
					regions.get(tripId),
					stopCounts.getOrDefault(tripId, 0L).intValue(),
					(int) totalCost
			));
		}
		return summaries;
	}

	// 여행마다 주소가 있는 첫 방문지(방문 순서)의 시 · 도
	private Map<Long, String> findRegions(Collection<Long> tripIds) {
		Map<Long, String> regions = new HashMap<>();
		List<Stop> stops = stopRepository.findByTripIdInAndAddressIsNotNullOrderByStopOrderAsc(tripIds);
		for (Stop stop : stops) {
			String region = RegionExtractor.extract(stop.getAddress());
			if (region != null) {
				regions.putIfAbsent(stop.getTrip().getId(), region);
			}
		}
		return regions;
	}

	private Map<Long, Long> toMap(List<TripIdAmount> rows) {
		Map<Long, Long> map = new HashMap<>();
		for (TripIdAmount row : rows) {
			map.put(row.getTripId(), row.getAmount());
		}
		return map;
	}
}

package kr.ync.triplan.share.dto.response;

import kr.ync.triplan.trip.domain.Lodging;
import kr.ync.triplan.trip.domain.Stop;
import kr.ync.triplan.trip.domain.TransportMode;
import kr.ync.triplan.trip.domain.TransportSegment;
import kr.ync.triplan.trip.domain.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// 공유 게시글로 공개되는 여행 정보. 예약번호는 개인 정보라 포함하지 않는다 (B12)
public record SharedTripResponse(
		Long tripId,
		String title,
		LocalDate startDate,
		LocalDate endDate,
		List<StopItem> stops,
		List<TransportSegmentItem> transportSegments,
		List<LodgingItem> lodgings,
		int transportCost,
		int lodgingCost,
		int totalCost
) {

	public record StopItem(
			Long id,
			String name,
			LocalDate date,
			LocalTime time,
			String memo,
			String imageUrl,
			Integer stopOrder,
			Double latitude,
			Double longitude,
			String address
	) {
		static StopItem from(Stop stop) {
			return new StopItem(
					stop.getId(), stop.getName(), stop.getDate(), stop.getTime(),
					stop.getMemo(), stop.getImageUrl(), stop.getStopOrder(),
					stop.getLatitude(), stop.getLongitude(), stop.getAddress()
			);
		}
	}

	public record TransportSegmentItem(
			Long id,
			Long fromStopId,
			Long toStopId,
			TransportMode mode,
			LocalDateTime departTime,
			LocalDateTime arriveTime,
			Integer cost
	) {
		static TransportSegmentItem from(TransportSegment segment) {
			return new TransportSegmentItem(
					segment.getId(), segment.getFromStop().getId(), segment.getToStop().getId(),
					segment.getMode(), segment.getDepartTime(), segment.getArriveTime(), segment.getCost()
			);
		}
	}

	public record LodgingItem(
			Long id,
			String name,
			LocalDateTime checkIn,
			LocalDateTime checkOut,
			Integer cost
	) {
		static LodgingItem from(Lodging lodging) {
			return new LodgingItem(
					lodging.getId(), lodging.getName(), lodging.getCheckIn(), lodging.getCheckOut(), lodging.getCost()
			);
		}
	}

	public static SharedTripResponse of(
			Trip trip, List<Stop> stops, List<TransportSegment> segments, List<Lodging> lodgings) {
		int transportCost = segments.stream()
				.mapToInt(segment -> segment.getCost() == null ? 0 : segment.getCost())
				.sum();
		int lodgingCost = lodgings.stream()
				.mapToInt(lodging -> lodging.getCost() == null ? 0 : lodging.getCost())
				.sum();

		return new SharedTripResponse(
				trip.getId(),
				trip.getTitle(),
				trip.getStartDate(),
				trip.getEndDate(),
				stops.stream().map(StopItem::from).toList(),
				segments.stream().map(TransportSegmentItem::from).toList(),
				lodgings.stream().map(LodgingItem::from).toList(),
				transportCost,
				lodgingCost,
				transportCost + lodgingCost
		);
	}
}

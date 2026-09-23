package kr.ync.triplan.service;

import kr.ync.triplan.domain.Stop;
import kr.ync.triplan.domain.TransportMode;
import kr.ync.triplan.domain.TransportSegment;
import kr.ync.triplan.domain.Trip;
import kr.ync.triplan.dto.request.TransportSegmentCreateRequest;
import kr.ync.triplan.dto.request.TransportSegmentUpdateRequest;
import kr.ync.triplan.dto.response.TransportSegmentResponse;
import kr.ync.triplan.exception.StopNotFoundException;
import kr.ync.triplan.exception.TransportSegmentNotFoundException;
import kr.ync.triplan.exception.TripNotFoundException;
import kr.ync.triplan.repository.StopRepository;
import kr.ync.triplan.repository.TransportSegmentRepository;
import kr.ync.triplan.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class TransportSegmentServiceImplTest {

	@Autowired
	private TransportSegmentService transportSegmentService;

	@Autowired
	private TransportSegmentRepository transportSegmentRepository;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private StopRepository stopRepository;

	private static final long NON_EXISTING_ID = 99999L;

	private Trip trip;
	private Stop fromStop;
	private Stop toStop;

	@BeforeEach
	void setUpFixture() {
		trip = tripRepository.save(
				Trip.builder()
						.title("제주도 여행")
						.startDate(LocalDate.now())
						.endDate(LocalDate.now().plusDays(3))
						.userId("user1")
						.build()
		);
		fromStop = stopRepository.save(
				Stop.builder().trip(trip).name("공항").date(LocalDate.now()).build());
		toStop = stopRepository.save(
				Stop.builder().trip(trip).name("숙소").date(LocalDate.now()).build());
	}

	private TransportSegment savedSegment() {
		return transportSegmentRepository.save(
				TransportSegment.builder()
						.trip(trip)
						.fromStop(fromStop)
						.toStop(toStop)
						.mode(TransportMode.CAR)
						.departTime(LocalDateTime.now())
						.arriveTime(LocalDateTime.now().plusHours(1))
						.build()
		);
	}

	@Test
	@DisplayName("create")
	void create_success() {
		// given
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), 5000, "RES123");
		// when
		TransportSegmentResponse response = transportSegmentService.create(trip.getId(), request);
		// then
		assertThat(response.id()).isNotNull();
		assertThat(response)
				.extracting(TransportSegmentResponse::mode, TransportSegmentResponse::fromStopId, TransportSegmentResponse::toStopId)
				.containsExactly(TransportMode.CAR, fromStop.getId(), toStop.getId());
	}

	@Test
	@DisplayName("create - 존재하지 않는 여행이면 예외")
	void create_tripNotFound() {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null);

		assertThatThrownBy(() -> transportSegmentService.create(NON_EXISTING_ID, request))
				.isInstanceOf(TripNotFoundException.class);
	}

	@Test
	@DisplayName("create - 존재하지 않는 방문지면 예외")
	void create_stopNotFound() {
		TransportSegmentCreateRequest request = new TransportSegmentCreateRequest(
				NON_EXISTING_ID, toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null);

		assertThatThrownBy(() -> transportSegmentService.create(trip.getId(), request))
				.isInstanceOf(StopNotFoundException.class);
	}

	@Test
	@DisplayName("getDetail")
	void getDetail_success() {
		// given
		TransportSegment saved = savedSegment();
		// when
		TransportSegmentResponse response = transportSegmentService.getDetail(saved.getId());
		// then
		assertThat(response.id()).isEqualTo(saved.getId());
	}

	@Test
	@DisplayName("getDetail - 존재하지 않으면 예외")
	void getDetail_notFound() {
		assertThatThrownBy(() -> transportSegmentService.getDetail(NON_EXISTING_ID))
				.isInstanceOf(TransportSegmentNotFoundException.class);
	}

	@Test
	@DisplayName("list")
	void list_success() {
		// given
		savedSegment();
		savedSegment();
		// when
		List<TransportSegmentResponse> list = transportSegmentService.getList(trip.getId());
		// then
		assertThat(list).hasSize(2);
	}

	@Test
	@DisplayName("update")
	void update_success() {
		// given
		TransportSegment saved = savedSegment();
		TransportSegmentUpdateRequest request = new TransportSegmentUpdateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.FLIGHT,
				LocalDateTime.now(), LocalDateTime.now().plusHours(2), 100000, "RES999");
		// when
		TransportSegmentResponse response = transportSegmentService.update(saved.getId(), request);
		// then
		assertThat(response.mode()).isEqualTo(TransportMode.FLIGHT);
		assertThat(response.cost()).isEqualTo(100000);
	}

	@Test
	@DisplayName("update - 존재하지 않으면 예외")
	void update_notFound() {
		TransportSegmentUpdateRequest request = new TransportSegmentUpdateRequest(
				fromStop.getId(), toStop.getId(), TransportMode.CAR,
				LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null);
		assertThatThrownBy(() -> transportSegmentService.update(NON_EXISTING_ID, request))
				.isInstanceOf(TransportSegmentNotFoundException.class);
	}

	@Test
	@DisplayName("delete")
	void delete_success() {
		// given
		TransportSegment saved = savedSegment();
		// when
		transportSegmentService.delete(saved.getId());
		// then
		assertThatThrownBy(() -> transportSegmentService.getDetail(saved.getId()))
				.isInstanceOf(TransportSegmentNotFoundException.class);
	}

	@Test
	@DisplayName("delete - notFound")
	void delete_notFound() {
		assertThatThrownBy(() -> transportSegmentService.delete(NON_EXISTING_ID))
				.isInstanceOf(TransportSegmentNotFoundException.class);
	}
}

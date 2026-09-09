package kr.ync.triplan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_segment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportSegment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_stop_id", nullable = false)
	private Stop fromStop;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_stop_id", nullable = false)
	private Stop toStop;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransportMode mode;

	@Column(nullable = false)
	private LocalDateTime departTime;

	@Column(nullable = false)
	private LocalDateTime arriveTime;

	private Integer cost;

	private String reservationNo;
}

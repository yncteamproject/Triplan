package kr.ync.triplan.share.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.ync.triplan.global.exception.ForbiddenException;
import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.trip.domain.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharePage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(length = 2000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member writer;

	@Column(nullable = false)
	private LocalDateTime writeDate;

	private LocalDateTime updateDate;

	@Column(nullable = false)
	@Builder.Default
	private int viewCount = 0;

	// 다른 사람이 이 게시글의 여행을 복사할 수 있는지. 기존 행 때문에 DB 기본값도 지정
	@Column(nullable = false)
	@ColumnDefault("true")
	@Builder.Default
	private boolean allowCopy = true;

	@Column(nullable = false)
	@ColumnDefault("0")
	@Builder.Default
	private int copyCount = 0;

	public void validateWriter(String email) {
		if (!writer.getEmail().equals(email)) {
			throw new ForbiddenException();
		}
	}

	// 복사를 허용한 게시글만 복사 가능. 작성자 본인은 항상 가능 (B11)
	public void validateCopyable(String email) {
		if (!allowCopy && !writer.getEmail().equals(email)) {
			throw new ForbiddenException();
		}
	}
}

package kr.ync.triplan.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelPreferenceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private TravelType travelType;

    private LocalDateTime testedAt;

    @Builder
    public TravelPreferenceResult(Member member, TravelType travelType) {
        this.member = member;
        this.travelType = travelType;
        this.testedAt = LocalDateTime.now();
    }
}
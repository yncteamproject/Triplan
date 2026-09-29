package kr.ync.triplan.travelTest.repository;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.travelTest.domain.TravelPreferenceResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelPreferenceResultRepository extends JpaRepository<TravelPreferenceResult, Long> {
    Optional<TravelPreferenceResult> findFirstByMemberOrderByIdDesc(Member member);
}
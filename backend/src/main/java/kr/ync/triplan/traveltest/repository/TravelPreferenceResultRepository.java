package kr.ync.triplan.traveltest.repository;

import kr.ync.triplan.member.domain.Member;
import kr.ync.triplan.traveltest.domain.TravelPreferenceResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelPreferenceResultRepository extends JpaRepository<TravelPreferenceResult, Long> {
    Optional<TravelPreferenceResult> findFirstByMemberOrderByIdDesc(Member member);
}
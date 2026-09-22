package kr.ync.tripplan.repository;

import kr.ync.tripplan.domain.TravelPreferenceResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPreferenceResultRepository extends JpaRepository<TravelPreferenceResult, Long> {
}
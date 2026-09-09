package kr.ync.triplan.repository;

import kr.ync.triplan.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

	List<Trip> findByUserId(String userId);
}

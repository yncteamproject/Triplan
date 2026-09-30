package kr.ync.triplan.trip.repository;

import kr.ync.triplan.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

	List<Trip> findByMemberEmail(String email);
}

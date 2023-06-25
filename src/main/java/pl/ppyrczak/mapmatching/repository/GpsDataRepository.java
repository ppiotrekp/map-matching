package pl.ppyrczak.mapmatching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ppyrczak.mapmatching.model.GpsData;

@Repository
public interface GpsRepository extends JpaRepository<GpsData, Long> {
}

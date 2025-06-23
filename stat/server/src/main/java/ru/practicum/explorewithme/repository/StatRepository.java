package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("""
        SELECT new ru.practicum.explorewithme.ViewStats(e.app, e.uri, COUNT(e))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND (:uris IS NULL OR e.uri IN :uris)
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
    """)
    List<ViewStats> getTotalStats(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("uris") List<String> uris);

    @Query("""
        SELECT new ru.practicum.explorewithme.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND (:uris IS NULL OR e.uri IN :uris)
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStats> getUniqueStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
}

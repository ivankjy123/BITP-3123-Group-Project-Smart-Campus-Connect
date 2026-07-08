package my.smartcampusconnect67.reporting_service.repository;

import my.smartcampusconnect67.reporting_service.model.AggregatedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregatedMetricRepository extends JpaRepository<AggregatedMetric, Long> {

    Optional<AggregatedMetric> findByMetricName(String metricName);

}
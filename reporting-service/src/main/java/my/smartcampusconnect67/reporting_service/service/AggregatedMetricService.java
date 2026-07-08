package my.smartcampusconnect67.reporting_service.service;

import my.smartcampusconnect67.reporting_service.model.AggregatedMetric;
import my.smartcampusconnect67.reporting_service.repository.AggregatedMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AggregatedMetricService {

    @Autowired
    private AggregatedMetricRepository repository;

    public List<AggregatedMetric> getAllMetrics() {
        return repository.findAll();
    }

    public AggregatedMetric saveMetric(String name, Integer value) {

        AggregatedMetric metric =
                repository.findByMetricName(name)
                        .orElse(new AggregatedMetric());

        metric.setMetricName(name);
        metric.setMetricValue(value);
        metric.setLastCalculatedAt(LocalDateTime.now());

        return repository.save(metric);
    }

}
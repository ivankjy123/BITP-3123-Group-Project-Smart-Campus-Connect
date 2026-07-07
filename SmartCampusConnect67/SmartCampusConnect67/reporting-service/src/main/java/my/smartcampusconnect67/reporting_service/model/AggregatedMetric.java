package my.smartcampusconnect67.reporting_service.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aggregated_metrics")
public class AggregatedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_name", unique = true, nullable = false)
    private String metricName;

    @Column(name = "metric_value")
    private Integer metricValue;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    public AggregatedMetric() {
    }

    public AggregatedMetric(Long id,
                            String metricName,
                            Integer metricValue,
                            LocalDateTime lastCalculatedAt) {
        this.id = id;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.lastCalculatedAt = lastCalculatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Integer getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Integer metricValue) {
        this.metricValue = metricValue;
    }

    public LocalDateTime getLastCalculatedAt() {
        return lastCalculatedAt;
    }

    public void setLastCalculatedAt(LocalDateTime lastCalculatedAt) {
        this.lastCalculatedAt = lastCalculatedAt;
    }
}
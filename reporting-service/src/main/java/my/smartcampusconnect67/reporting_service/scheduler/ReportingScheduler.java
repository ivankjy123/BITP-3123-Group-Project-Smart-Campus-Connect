package my.smartcampusconnect67.reporting_service.scheduler;

import my.smartcampusconnect67.reporting_service.dto.DashboardReportDTO;
import my.smartcampusconnect67.reporting_service.service.AggregatedMetricService;
import my.smartcampusconnect67.reporting_service.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportingScheduler {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private AggregatedMetricService metricService;

    @Scheduled(fixedRate = 60000)
    public void refreshMetrics() {

        DashboardReportDTO dashboard =
                reportingService.getDashboardReport();

        metricService.saveMetric("TOTAL_STUDENTS",
                dashboard.getTotalStudents());

        metricService.saveMetric("TOTAL_ENROLMENTS",
                dashboard.getTotalEnrolments());

        metricService.saveMetric("TOTAL_BOOK_LOANS",
                dashboard.getTotalBooksBorrowed());

        metricService.saveMetric("TOTAL_ACTIVE_STUDENTS",
                dashboard.getTotalActiveStudents());

        System.out.println("Metrics refreshed automatically.");
    }
}
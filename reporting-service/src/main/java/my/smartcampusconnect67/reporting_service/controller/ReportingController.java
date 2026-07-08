package my.smartcampusconnect67.reporting_service.controller;

import my.smartcampusconnect67.reporting_service.dto.DashboardReportDTO;
import my.smartcampusconnect67.reporting_service.dto.ProgrammeReportDTO;
import my.smartcampusconnect67.reporting_service.model.AggregatedMetric;
import my.smartcampusconnect67.reporting_service.service.AggregatedMetricService;
import my.smartcampusconnect67.reporting_service.service.ReportingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import my.smartcampusconnect67.reporting_service.error.ErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private AggregatedMetricService metricService;

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {

        try {

            DashboardReportDTO report =
                    reportingService.getDashboardReport();

            return ResponseEntity.ok(report);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            e.getMessage(),
                            "/api/reports/dashboard"
                    ));
        }

    }

    // Students by Programme
    @GetMapping("/programmes")
    public ResponseEntity<List<ProgrammeReportDTO>> getProgrammeReport() {

        return ResponseEntity.ok(
                reportingService.getProgrammeReport()
        );
    }

    // Read metrics from Reporting Database
    @GetMapping("/metrics")
    public ResponseEntity<List<AggregatedMetric>> getMetrics() {

        return ResponseEntity.ok(
                metricService.getAllMetrics()
        );
    }

    // Refresh metrics
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshDashboard() {

        DashboardReportDTO dashboard =
                reportingService.getDashboardReport();

        metricService.saveMetric(
                "TOTAL_STUDENTS",
                dashboard.getTotalStudents());

        metricService.saveMetric(
                "TOTAL_ENROLMENTS",
                dashboard.getTotalEnrolments());

        metricService.saveMetric(
                "TOTAL_BOOK_LOANS",
                dashboard.getTotalBooksBorrowed());

        metricService.saveMetric(
                "TOTAL_ACTIVE_STUDENTS",
                dashboard.getTotalActiveStudents());

        return ResponseEntity.ok(
                "Dashboard metrics refreshed successfully."
        );
    }

}
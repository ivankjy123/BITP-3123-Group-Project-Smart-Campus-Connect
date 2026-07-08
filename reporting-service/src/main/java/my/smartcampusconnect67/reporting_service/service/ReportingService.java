package my.smartcampusconnect67.reporting_service.service;

import my.smartcampusconnect67.reporting_service.dto.DashboardReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import my.smartcampusconnect67.reporting_service.dto.StudentDTO;
import my.smartcampusconnect67.reporting_service.dto.ProgrammeReportDTO;
import my.smartcampusconnect67.reporting_service.client.StudentClient;
import my.smartcampusconnect67.reporting_service.client.EnrolmentClient;
import my.smartcampusconnect67.reporting_service.client.LibraryClient;
import java.util.*;

@Service
public class ReportingService {
	
	@Autowired
	private StudentClient studentClient;

	@Autowired
	private EnrolmentClient enrolmentClient;

	@Autowired
	private LibraryClient libraryClient;

    public DashboardReportDTO getDashboardReport() {

    	StudentDTO[] students = studentClient.getStudents();

    	Object[] enrolments = enrolmentClient.getEnrolments();
    	
    	Object[] courses = enrolmentClient.getCourses();

    	Object[] loans = libraryClient.getLoans();
    	
        DashboardReportDTO dashboard = new DashboardReportDTO();

        dashboard.setTotalStudents(
                students == null ? 0 : students.length);

        dashboard.setTotalEnrolments(
                enrolments == null ? 0 : enrolments.length);

        dashboard.setTotalBooksBorrowed(
                loans == null ? 0 : loans.length);

        dashboard.setTotalCourses(
                courses == null ? 0 : courses.length
        );
        
        int activeStudents = 0;
        
        if (students != null) {
            for (StudentDTO student : students) {
                if ("ACTIVE".equalsIgnoreCase(student.getStatus())) {
                    activeStudents++;
                }
            }
        }

        dashboard.setTotalActiveStudents(activeStudents);

        return dashboard;
    }
    
    public List<ProgrammeReportDTO> getProgrammeReport() {

    	StudentDTO[] students = studentClient.getStudents();

        Map<String, Integer> countMap = new HashMap<>();

        if (students != null) {

            for (StudentDTO student : students) {

                String programme = student.getProgramme();

                countMap.put(
                        programme,
                        countMap.getOrDefault(programme, 0) + 1
                );
            }
        }

        List<ProgrammeReportDTO> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {

            result.add(
                    new ProgrammeReportDTO(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return result;
    }
}
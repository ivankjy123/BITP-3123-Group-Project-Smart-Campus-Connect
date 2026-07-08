package my.smartcampusconnect67.reporting_service.client;

import my.smartcampusconnect67.reporting_service.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StudentClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String STUDENT_URL =
            "http://localhost:8081/api/students";

    public StudentDTO[] getStudents() {

        return restTemplate.getForObject(
                STUDENT_URL,
                StudentDTO[].class
        );

    }

}
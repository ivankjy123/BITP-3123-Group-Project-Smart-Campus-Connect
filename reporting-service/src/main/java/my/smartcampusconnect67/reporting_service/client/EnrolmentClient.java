package my.smartcampusconnect67.reporting_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EnrolmentClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ENROLMENT_URL =
            "http://localhost:8082/api/enrolments";

    private static final String COURSE_URL =
            "http://localhost:8082/api/enrolments/courses";

    public Object[] getEnrolments() {

        return restTemplate.getForObject(
                ENROLMENT_URL,
                Object[].class
        );

    }

    public Object[] getCourses() {

        return restTemplate.getForObject(
                COURSE_URL,
                Object[].class
        );

    }
}
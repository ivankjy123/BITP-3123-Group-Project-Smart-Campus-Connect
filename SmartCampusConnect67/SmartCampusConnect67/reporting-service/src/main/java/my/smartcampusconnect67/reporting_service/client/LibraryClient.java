package my.smartcampusconnect67.reporting_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LibraryClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String LOAN_URL =
            "http://localhost:8084/api/loans";

    public Object[] getLoans() {

        return restTemplate.getForObject(
                LOAN_URL,
                Object[].class
        );

    }

}
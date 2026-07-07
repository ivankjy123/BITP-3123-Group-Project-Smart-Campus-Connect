package my.smartcampusconnect67.enrolment_service;

import my.smartcampusconnect67.enrolment_service.model.Enrolment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/before-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/after-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class EnrolmentLoadTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();   // manual creation
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/enrolments";
    }

    @Test
    void concurrentEnrolmentsMustNotExceedCapacity() throws InterruptedException {
        int threadCount = 50;
        int maxCapacity = 25;   // must match the capacity set in before-test.sql
        String courseCode = "LOADTEST";
        String matricNoPrefix = "B_LOADTEST";

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger capacityRejectedCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Enrolment enrolment = new Enrolment();
                    enrolment.setStudentMatricNo(matricNoPrefix + index);
                    enrolment.setCourseCode(courseCode);

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            baseUrl, enrolment, String.class
                    );

                    System.out.println("Response for " + enrolment.getStudentMatricNo() + ": " + response.getStatusCode() + " - " + response.getBody());
                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                    }
                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    // RestTemplate throws this exception for 4xx and 5xx responses
                    String responseBody = e.getResponseBodyAsString();
                    System.out.println("Rejected for index " + index + ": " + e.getStatusCode() + " - " + responseBody);

                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                            responseBody != null &&
                            responseBody.contains("Course is fulled")) {
                        capacityRejectedCount.incrementAndGet(); // This will now increment correctly!
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected request failure: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        //exactly max capacity enrolments should succeed while the reset is rejected.
        int expectedSuccess = maxCapacity;
        int expectedRejections = threadCount - maxCapacity;

        int actualSuccess = successCount.get();
        int actualRejections = capacityRejectedCount.get();

        // 2. Print a human-readable visual dashboard to the console
        System.out.println("\n==================================================");
        System.out.println("               LOAD TEST SUMMARY                  ");
        System.out.println("==================================================");
        System.out.println("Total Requests Sent : " + threadCount);
        System.out.println("SUCCESSFUL ENROLMENTS -> Expected: " + expectedSuccess + " | Actual: " + actualSuccess);
        System.out.println("REJECTED (FULL MAP)   -> Expected: " + expectedRejections + " | Actual: " + actualRejections);
        System.out.println("==================================================\n");

        // 3. Assert with descriptive custom error messages if they fail
        assertThat(actualSuccess)
                .withFailMessage("SUCCESS COUNT MISMATCH! Expected exactly %d successes but got %d.", expectedSuccess, actualSuccess)
                .isEqualTo(expectedSuccess);

        assertThat(actualRejections)
                .withFailMessage("REJECTION COUNT MISMATCH! Expected exactly %d rejections due to capacity but got %d.", expectedRejections, actualRejections)
                .isEqualTo(expectedRejections);
    }
}
package my.smartcampusconnect67.library_service.service;

import my.smartcampusconnect67.library_service.client.NotificationTcpClient;
import my.smartcampusconnect67.library_service.dto.NotificationMessage;
import my.smartcampusconnect67.library_service.model.BookLoan;
import my.smartcampusconnect67.library_service.repository.BookLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanService {
    private static final String STUDENT_PROFILE_URL = "http://localhost:8081/api/students";
    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private BookLoanRepository loanRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private NotificationTcpClient notificationClient;

    public List<BookLoan> getLoansByStudent(String matricNo) {
        return loanRepository.findByStudentMatricNo(matricNo);
    }

    public List<BookLoan> getAllLoans() {
        return loanRepository.findAll();
    }

    public BookLoan borrowBook(String studentMatricNo, String isbn) {
        // Validate student existence
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + studentMatricNo))
                .GET()
                .build()
        ;
        String studentEmail = null;

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student with matric No " + studentMatricNo + " not found"
                );
            }

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Unable to contact Student Service."
                );
            }

            // Extract email from JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // Check Student Status
            JsonNode statusNode = root.get("status");
            String studentStatus = (statusNode != null) ? statusNode.asString() : null;
            if (!"ACTIVE".equalsIgnoreCase(studentStatus)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Student with matric No " + studentMatricNo + " is not active (status: " + studentStatus + "). Cannot borrow book."
                );
            }

            // Get Email if Active
            JsonNode emailNode = root.get("email");
            studentEmail = (emailNode != null) ? emailNode.asString() : null;

            bookService.decreaseAvailableCopies(isbn);
        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Student Service is unavailable."
            );
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        // Create Loan
        BookLoan loan = new BookLoan();
        loan.setStudentMatricNo(studentMatricNo);
        loan.setIsbn(isbn);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus("BORROWED");
        loan = loanRepository.save(loan);

        // Send notification asynchronously
        if (studentEmail != null) {
            try {
                NotificationMessage msg = new NotificationMessage(
                        "BOOK_LOAN",
                        studentEmail,
                        "You have borrowed ISBN: " + isbn,
                        System.currentTimeMillis()
                );
                notificationClient.send(msg);
            } catch (Exception e) {
                System.err.println("Notification failed: " + e.getMessage());
            }
        }

        return loan;
    }

    public BookLoan returnBook(Integer loanId) {
        String studentEmail = null;
        HttpRequest request;

        BookLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan not found"
                )
        );

        if (!"BORROWED".equals(loan.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Loan is not active"
            );
        }
        loan.setStatus("RETURNED");
        loan = loanRepository.save(loan);

        bookService.increaseAvailableCopies(loan.getIsbn());

        request = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + loan.getStudentMatricNo()))
                .GET()
                .build()
        ;

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode emailNode = root.get("email");
                studentEmail = (emailNode != null) ? emailNode.asString() : null;
            }

            if (studentEmail != null) {
                NotificationMessage msg = new NotificationMessage(
                        "BOOK_RETURN",
                        studentEmail,
                        "You have returned ISBN: " + loan.getIsbn(),
                        System.currentTimeMillis()
                );
                notificationClient.send(msg);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Notification failed: " + e.getMessage());
        }

        return loan;
    }
}
package my.smartcampusconnect67.library_service.controller;

import my.smartcampusconnect67.library_service.model.BookLoan;
import my.smartcampusconnect67.library_service.service.BookService;
import my.smartcampusconnect67.library_service.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public List<BookLoan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/student/{matricNo}")
    public List<BookLoan> getLoansByStudent(@PathVariable String matricNo) {
        return loanService.getLoansByStudent(matricNo);
    }

    @PostMapping
    public ResponseEntity<?> borrowBook(@RequestParam String studentMatricNo, @RequestParam String isbn) {
        try {
            BookLoan loan = loanService.borrowBook(studentMatricNo, isbn);
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{loanId}/return")
    public ResponseEntity<?> returnBook(@PathVariable Integer loanId) {
        try {
            BookLoan loan = loanService.returnBook(loanId);
            return ResponseEntity.ok(loan);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

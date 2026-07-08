package my.smartcampusconnect67.library_service.repository;

import my.smartcampusconnect67.library_service.model.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookLoanRepository extends JpaRepository<BookLoan, Integer> {
    List<BookLoan> findByStudentMatricNo(String matricNo);
    List<BookLoan> findByIsbn(String isbn);
    List<BookLoan> findByStatus(String status);
    List<BookLoan> findByIsbnAndStatusIn(String isbn, List<String> statuses);
}

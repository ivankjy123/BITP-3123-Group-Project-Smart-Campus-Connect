package my.smartcampusconnect67.library_service.service;

import my.smartcampusconnect67.library_service.model.Book;
import my.smartcampusconnect67.library_service.model.BookLoan;
import my.smartcampusconnect67.library_service.repository.BookLoanRepository;
import my.smartcampusconnect67.library_service.repository.BookRepository;
import my.smartcampusconnect67.library_service.soap.BookHasActiveLoansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookLoanRepository bookLoanRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
    }

    public Book addBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + book.getIsbn() + " already exists");
        } else if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book can't have available copies less then 1. You inserted: " + book.getAvailableCopies());
        }
        return bookRepository.save(book);
    }

    public Book updateBook(String isbn, Book updated) {
        if (updated.getAvailableCopies() < 0) {
            throw new RuntimeException("Book can't have available copies less then 0 (negative). You inserted: " + updated.getAvailableCopies());
        }

        Book existing = getBookByIsbn(isbn);
        existing.setTitle(updated.getTitle());
        existing.setAvailableCopies(updated.getAvailableCopies());
        return bookRepository.save(existing);
    }

    public void deleteBook(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found: " + isbn));

        // Check for active loans (BORROWED, OVERDUE)
        List<BookLoan> activeLoans = bookLoanRepository.findByIsbnAndStatusIn(
                isbn,
                List.of("BORROWED", "OVERDUE")
        );
        if (!activeLoans.isEmpty()) {
            throw new BookHasActiveLoansException(
                    "Cannot delete book with ISBN " + isbn + " as book has active loan.",
                    "Book has " + activeLoans.size() + " active loan(s). Return all copies first."
            );
        }
        bookRepository.delete(book);
    }

    public boolean isBookAvailable(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(book -> book.getAvailableCopies() > 0)
                .orElse(false);
    }

    // For loan service (Add/Decease # Available books)
    public void decreaseAvailableCopies(String isbn) {
        Book book = getBookByIsbn(isbn);
        if (book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No copies available for loan"
            );
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
    }

    public void increaseAvailableCopies(String isbn) {
        Book book = getBookByIsbn(isbn);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }
}

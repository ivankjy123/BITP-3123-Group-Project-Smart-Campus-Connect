package my.smartcampusconnect67.library_service.soap;

import jakarta.jws.WebService;
import my.smartcampusconnect67.library_service.model.Book;
import my.smartcampusconnect67.library_service.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@WebService(endpointInterface = "my.smartcampusconnect67.library_service.soap.LibraryService")
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    private BookService bookService;

    @Override
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        try {
            return bookService.getBookByIsbn(isbn);
        } catch (RuntimeException e) {
            throw new BookNotFoundException("Book not found: " + isbn, e.getMessage());
        }
    }

    @Override
    public Book addBook(Book book) {
        try {
            return bookService.addBook(book);
        } catch (RuntimeException e) {
            throw new BookNotFoundException("Could not add book: " + e.getMessage(), e.getMessage());
        }
    }

    @Override
    public Book updateBook(String isbn, Book book) {
        try {
            return bookService.updateBook(isbn, book);
        } catch (RuntimeException e) {
            throw new BookNotFoundException("Update failed: " + e.getMessage(), e.getMessage());
        }
    }

    @Override
    public boolean deleteBook(String isbn) {
        try {
            bookService.deleteBook(isbn);
            return true;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public boolean isBookAvailable(String isbn) {
        return bookService.isBookAvailable(isbn);
    }
}

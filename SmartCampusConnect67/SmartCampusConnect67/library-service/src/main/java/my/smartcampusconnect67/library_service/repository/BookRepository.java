package my.smartcampusconnect67.library_service.repository;

import my.smartcampusconnect67.library_service.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
}

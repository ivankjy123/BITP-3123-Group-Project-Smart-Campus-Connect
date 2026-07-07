package my.smartcampusconnect67.library_service.soap;

import my.smartcampusconnect67.library_service.model.Book;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

@WebService
public interface LibraryService {

    @WebMethod
    List<Book> getAllBooks();

    @WebMethod
    Book getBookByIsbn(@WebParam(name = "isbn") String isbn);

    @WebMethod
    Book addBook(@WebParam(name = "book") Book book);

    @WebMethod
    Book updateBook(@WebParam(name = "isbn") String isbn, @WebParam(name = "book") Book book);

    @WebMethod
    boolean deleteBook(@WebParam(name = "isbn") String isbn);

    @WebMethod
    boolean isBookAvailable(@WebParam(name = "isbn") String isbn);
}

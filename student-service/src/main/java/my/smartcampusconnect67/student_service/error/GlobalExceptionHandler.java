package my.smartcampusconnect67.student_service.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJsonFields(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String detailMessage = "Malformed JSON payload or invalid fields provided.";

        //retrieve the field name that causes error
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedEx) {
            detailMessage = "Invalid field name: '" + unrecognizedEx.getPropertyName() + "' is not recognized.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Bad Request",
                        HttpStatus.BAD_REQUEST.value(),
                        detailMessage,
                        request.getRequestURI()
                ));
    }
}
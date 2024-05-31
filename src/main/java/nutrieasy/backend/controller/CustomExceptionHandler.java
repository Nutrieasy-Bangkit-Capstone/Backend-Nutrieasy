package nutrieasy.backend.controller;

/**
 * Created by Resa S.
 * Date: 04-05-2024
 * Created in IntelliJ IDEA.
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Created by Resa S.
 * Date: 25-04-2024
 * Created in IntelliJ IDEA.
 */
@ControllerAdvice
public class CustomExceptionHandler {


    // 403 Forbidden
    @ExceptionHandler({AccessDeniedException.class})
    public final ResponseEntity<String> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler({Exception.class})
    public final ResponseEntity<String> handleException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}


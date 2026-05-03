package youngju.seonbimind.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(SEOUL_ZONE),
                statusCode.value(),
                getReasonPhrase(statusCode),
                exception.getReason(),
                request.getRequestURI()
        );

        return ResponseEntity.status(statusCode).body(response);
    }

    private String getReasonPhrase(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus httpStatus) {
            return httpStatus.getReasonPhrase();
        }
        return statusCode.toString();
    }

    public record ApiErrorResponse(
            LocalDateTime timestamp,
            Integer status,
            String error,
            String message,
            String path
    ) {
    }
}

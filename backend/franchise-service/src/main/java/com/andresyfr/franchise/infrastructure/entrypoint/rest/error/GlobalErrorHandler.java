package com.andresyfr.franchise.infrastructure.entrypoint.rest.error;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import com.andresyfr.franchise.domain.exception.BusinessRuleViolationException;
import com.andresyfr.franchise.domain.exception.ResourceAlreadyExistsException;
import com.andresyfr.franchise.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

/**
 * Translates exceptions into RFC 9457 Problem Details with a stable {@code code}.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private static final String CODE_PROPERTY = "code";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String ERRORS_PROPERTY = "errors";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private final Clock clock;

    public GlobalErrorHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException exception) {
        return respond(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getCode());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleResourceAlreadyExists(ResourceAlreadyExistsException exception) {
        return respond(HttpStatus.CONFLICT, exception.getMessage(), exception.getCode());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleViolationException exception) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), exception.getCode());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleValidation(WebExchangeBindException exception) {
        List<FieldViolation> violations = exception.getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "The request contains invalid fields", VALIDATION_ERROR);
        problem.setProperty(ERRORS_PROPERTY, violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDetail> handleMalformedRequest(ServerWebInputException exception) {
        return respond(HttpStatus.BAD_REQUEST, "The request body or parameters are malformed", MALFORMED_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        return respond(status, exception.getReason(), "HTTP_" + status.value());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while processing request", exception);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", INTERNAL_ERROR);
    }

    private ResponseEntity<ProblemDetail> respond(HttpStatusCode status, String detail, String code) {
        return ResponseEntity.status(status).body(problem(status, detail, code));
    }

    private ProblemDetail problem(HttpStatusCode status, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE_PROPERTY, code);
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now(clock).toString());
        return problem;
    }
}
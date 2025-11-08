package com.example.social.controller.error;

import com.example.social.dto.response.error.ResErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler({ResourceAlreadyExistsException.class,
            ResourceNotFoundException.class,
            UsernameNotFoundException.class})
    public ResponseEntity<ResErrorDTO> handleAllExceptions(Exception ex, HttpServletRequest request) {
        ResErrorDTO resErrorDTO = mapErrorDTO(ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resErrorDTO);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            SQLIntegrityConstraintViolationException.class
    })
    public ResponseEntity<ResErrorDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Group errors by field name
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.joining(", ")
                        )
                ));

        // Format: "field: message1, message2"
        List<String> errorMessages = errors.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());

        Object message = errorMessages.size() == 1 ? errorMessages.get(0) : errorMessages;

        ResErrorDTO resErrorDTO = ResErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resErrorDTO);
    }

    public ResErrorDTO mapErrorDTO(Exception ex, HttpServletRequest request) {
        return ResErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }
}

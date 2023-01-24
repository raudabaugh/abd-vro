package gov.va.vro.mockbipce.api;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.VefsErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles method argument not valid.
   *
   * @param exception the exception
   * @return returns exception
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<VefsErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    log.error("Validation error", exception);
    final StringBuffer errors = new StringBuffer();
    for (final FieldError error : exception.getBindingResult().getFieldErrors()) {
      if (!errors.isEmpty()) {
        errors.append("\n");
      }
      errors.append(error.getField() + ": " + error.getDefaultMessage());
    }
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage(errors.toString());
    return new ResponseEntity<>(cpe, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles method argument not valid type.
   *
   * @param exception the exception
   * @return returns exception
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<VefsErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentTypeMismatchException exception) {
    log.error("Validation error", exception);
    MethodParameter parameter = exception.getParameter();
    String name = parameter.getParameterName() + " is of wrong type.";
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage(name);
    return new ResponseEntity<>(cpe, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles constraint violations such as min or max limits.
   *
   * @param exception the exception
   * @return returns exception
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<VefsErrorResponse> handleMethodArgumentNotValidException(
      ConstraintViolationException exception) {
    log.error("Validation error", exception);
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage("invalid parameters");
    return new ResponseEntity<>(cpe, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles JSON parsing.
   *
   * @param exception the exception
   * @return returns exception
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<VefsErrorResponse> handleJsonParseException(JsonParseException exception) {
    log.error("Bad Request: Malformed JSON", exception);
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
    return new ResponseEntity<>(cpe, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles unsupported HTTP Methods.
   *
   * @param exception the exception
   * @return new exception
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<VefsErrorResponse> handleUnsupportedHttpMethodException(
      HttpRequestMethodNotSupportedException exception) {
    log.error("HTTP Method Not Supported");
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
    return new ResponseEntity<VefsErrorResponse>(cpe, HttpStatus.METHOD_NOT_ALLOWED);
  }

  /**
   * Handles general, unspecified exceptions (catch-all).
   *
   * @param exception the exception
   * @return returns exception
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<VefsErrorResponse> handleException(Exception exception) {
    log.error("Unexpected error", exception);
    VefsErrorResponse cpe = new VefsErrorResponse();
    cpe.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    return new ResponseEntity<>(cpe, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
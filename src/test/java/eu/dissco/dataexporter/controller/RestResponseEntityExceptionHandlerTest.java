package eu.dissco.dataexporter.controller;

import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.dataexporter.exception.ForbiddenException;
import eu.dissco.dataexporter.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RestResponseEntityExceptionHandlerTest {

  private RestResponseEntityExceptionHandler exceptionHandler;

  @BeforeEach
  void setup() {
    exceptionHandler = new RestResponseEntityExceptionHandler();
  }

  @Test
  void testForbiddenException() {
    // When
    var result = exceptionHandler.forbiddenException(new ForbiddenException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void testInvalidRequestException() {
    // When
    var result = exceptionHandler.invalidRequestException(new InvalidRequestException(""));

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }


}

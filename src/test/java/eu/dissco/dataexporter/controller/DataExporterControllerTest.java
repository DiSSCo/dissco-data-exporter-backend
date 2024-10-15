package eu.dissco.dataexporter.controller;

import static eu.dissco.dataexporter.utils.TestUtils.EMAIL;
import static eu.dissco.dataexporter.utils.TestUtils.ID;
import static eu.dissco.dataexporter.utils.TestUtils.ORCID;
import static eu.dissco.dataexporter.utils.TestUtils.givenClaims;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobRequest;
import static eu.dissco.dataexporter.utils.TestUtils.givenUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import eu.dissco.dataexporter.exception.ForbiddenException;
import eu.dissco.dataexporter.service.DataExporterService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class DataExporterControllerTest {

  @Mock
  private DataExporterService service;
  @Mock
  private Authentication authentication;
  private DataExporterController controller;

  @BeforeEach
  void setup() {
    controller = new DataExporterController(service);
  }

  @Test
  void testScheduleJob() throws Exception {
    // Given
    givenAuthentication(givenClaims());

    // When
    var result = controller.scheduleJob(authentication, givenJobRequest());

    // Then
    then(service).should().addJobToQueue(givenJobRequest(), givenUser());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void testMarkJobAsRunning() {
    // When
    var result = controller.markJobAsRunning(ID);

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void testScheduleJobNoOrcid() {
    // Given
    givenAuthentication(Map.of("email", EMAIL));

    // When / Then
    assertThrowsExactly(ForbiddenException.class,
        () -> controller.scheduleJob(authentication, givenJobRequest()));
  }

  @Test
  void testScheduleJobNoEmail() {
    // Given
    givenAuthentication(Map.of(
        "orcid", ORCID
    ));

    // When / Then
    assertThrowsExactly(ForbiddenException.class,
        () -> controller.scheduleJob(authentication, givenJobRequest()));
  }

  private void givenAuthentication(Map<String, Object> claims) {
    var principal = mock(Jwt.class);
    given(authentication.getPrincipal()).willReturn(principal);
    given(principal.getClaims()).willReturn(claims);
  }

}

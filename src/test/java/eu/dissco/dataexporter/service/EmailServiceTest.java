package eu.dissco.dataexporter.service;

import static eu.dissco.dataexporter.utils.TestUtils.EMAIL;
import static eu.dissco.dataexporter.utils.TestUtils.MAPPER;
import static eu.dissco.dataexporter.utils.TestUtils.SUBJECT;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
import static eu.dissco.dataexporter.utils.TestUtils.givenScheduledJob;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.dataexporter.database.jooq.enums.JobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {


  private EmailService emailService;
  @Mock
  private SesV2Client emailClient;

  private static final String SUCCESSFUL_EMAIL = """
      Good day,
      <p>
      Your DiSSCo download job is ready at the following link: https://aws.download/s3
      <p><p>
      Warm regards,
      <p>
      The DiSSCo development team
      """;

  private static final String NOT_FOUND_EMAIL = """
      Good day,
      <p>
      You requested a DiSSCo data export based on the following criteria:
      <p>
      {"$['ods:organisationID']":"https://ror.org/0566bfb96"}
      These search parameters yielded no results. Consider broadening your search.
      <p><p>
      Warm regards,
      <p>
      The DiSSCo development team
      """;

  @BeforeEach
  void setup() {
    emailService = new EmailService(emailClient, MAPPER);
  }

  @Test
  void testSendEmail() {
    // Given
    var jobResult = givenJobResult();
    var expected = givenEmailRequest(SUCCESSFUL_EMAIL);

    // When
    emailService.sendAwsMail(jobResult.downloadLink(), givenScheduledJob());

    // Then
    then(emailClient).should().sendEmail(expected);
  }

  @Test
  void testSendEmailFailed() {
    // Given
    given(emailClient.sendEmail(givenEmailRequest(SUCCESSFUL_EMAIL))).willThrow(SesV2Exception.class);

    // When
    var result = emailService.sendAwsMail(givenJobResult().downloadLink(), givenScheduledJob());

    // Then
    assertThat(result).isEqualTo(JobState.NOTIFICATION_FAILED);
  }

  @Test
  void testSendEmailEmptyResults() {
    // Given
    var expected = givenEmailRequest(NOT_FOUND_EMAIL);

    // When
    emailService.sendAwsMail(null, givenScheduledJob());

    // Then
    then(emailClient).should().sendEmail(expected);
  }

  private SendEmailRequest givenEmailRequest(String emailContent) {
    return SendEmailRequest.builder()
        .destination(destination -> destination
            .toAddresses(EMAIL).build())
        .content(content -> content.simple(
            message -> message
                .subject(subject -> subject.data(SUBJECT))
                .body(body -> body
                    .html(bodyContent -> bodyContent
                        .data(emailContent)))))
        .fromEmailAddress("no.reply.dissco@gmail.com")
        .build();
  }



}

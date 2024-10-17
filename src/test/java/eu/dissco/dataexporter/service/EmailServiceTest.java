package eu.dissco.dataexporter.service;

import static eu.dissco.dataexporter.utils.TestUtils.EMAIL;
import static eu.dissco.dataexporter.utils.TestUtils.SUBJECT;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
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

  @BeforeEach
  void setup() {
    emailService = new EmailService(emailClient);
  }

  @Test
  void testSendEmail() {
    // Given
    var jobResult = givenJobResult();
    var expected = givenEmailRequest();

    // When
    emailService.sendAwsMail(jobResult.downloadLink(), EMAIL);

    // Then
    then(emailClient).should().sendEmail(expected);
  }

  @Test
  void testSendEmailFailed() {
    // Given
    given(emailClient.sendEmail(givenEmailRequest())).willThrow(SesV2Exception.class);

    // When
    var result = emailService.sendAwsMail(givenJobResult().downloadLink(), EMAIL);

    // Then
    assertThat(result).isEqualTo(JobState.NOTIFICATION_FAILED);
  }

  private SendEmailRequest givenEmailRequest() {
    return SendEmailRequest.builder()
        .destination(destination -> destination
            .toAddresses(EMAIL).build())
        .content(content -> content.simple(
            message -> message
                .subject(subject -> subject.data(SUBJECT))
                .body(body -> body
                    .html(bodyContent -> bodyContent
                        .data(
                            """
                                Good day,
                                <p>
                                Your DiSSCo download job is ready at the following link: https://aws.download/s3
                                <p><p>
                                Warm regards,
                                <p>
                                The DiSSCo development team
                                """)))))
        .fromEmailAddress("no.reply.dissco@gmail.com")
        .build();
  }
}

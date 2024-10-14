package eu.dissco.dataexporter.service;

import static eu.dissco.dataexporter.utils.TestUtils.EMAIL;
import static eu.dissco.dataexporter.utils.TestUtils.givenEmailMessage;
import static eu.dissco.dataexporter.utils.TestUtils.givenJobResult;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.dissco.dataexporter.properties.AwsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@ExtendWith(MockitoExtension.class)
class AwsEmailServiceTest {


  private AwsEmailService emailService;
  @Mock
  private SesV2Client emailClient;

  @BeforeEach
  void setup(){
    emailService = new AwsEmailService(emailClient, new AwsProperties());
  }

  @Test
  void testSendEmail(){
    // Given
    var jobResult = givenJobResult();
    var expected = givenEmailRequest();

    // When
    emailService.sendMail(jobResult);

    // Then
    then(emailClient).should().sendEmail(expected);
  }

  @Test
  void testSendEmailFailed(){
    // Given
    var jobResult = givenJobResult();
    given(emailClient.sendEmail(givenEmailRequest())).willThrow(SesV2Exception.class);

    // When / Then
    assertThrows(SesV2Exception.class, () -> emailService.sendMail(jobResult));
  }

  private SendEmailRequest givenEmailRequest(){
    return SendEmailRequest.builder()
        .destination(destination -> destination.toAddresses(EMAIL).build())
        .content(content -> content.template(
                template -> template
                    .templateName("job-results")
                    .templateData(givenEmailMessage())
                    .build())
            .build())
        .fromEmailAddress("no.reply.dissco@gmail.com")
        .build();
  }

}

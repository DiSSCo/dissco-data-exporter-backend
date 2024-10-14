package eu.dissco.dataexporter.service;

import eu.dissco.dataexporter.domain.JobResult;
import eu.dissco.dataexporter.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsEmailService {

  private final SesV2Client emailClient;
  private final AwsProperties awsProperties;

  public void sendMail(JobResult jobResult) {
    var emailRequest = SendEmailRequest.builder()
        .destination(destination -> destination.toAddresses(jobResult.destinationEmail()).build())
        .content(content -> content.template(
                template -> template
                    .templateName(awsProperties.getEmailTemplateName())
                    .templateData(String.format(awsProperties.getEmailTemplate(), jobResult.s3Link()))
                    .build())
            .build())
        .fromEmailAddress(awsProperties.getSender())
        .build();
    try {
      emailClient.sendEmail(emailRequest);
      log.info("Successfully notified user of successful job");
    } catch (SesV2Exception e) {
      log.error("Unable to send email to recipient. Job result: {}", jobResult, e);
      throw e;
    }
  }

}

package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NotBlank
@ConfigurationProperties("application")
public class AwsProperties {
  @NotBlank
  String emailTemplateName = "job-results";
  @NotBlank
  String sender = "no.reply.dissco@gmail.com";
  @NotBlank
  String emailTemplate = """
      Good day,
      
      Your DiSSCo download job is ready at the following link: %s
      
      Warm regards,
      The DiSSCo development team
      """;

}

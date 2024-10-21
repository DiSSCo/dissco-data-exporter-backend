package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NotBlank
@ConfigurationProperties("aws")
public class AwsProperties {

  @NotBlank
  private String accessKey;

  private String secretAccessKey;

}

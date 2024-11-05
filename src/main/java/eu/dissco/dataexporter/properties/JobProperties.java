package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("job")
public class JobProperties {

  @Positive
  Integer queueSize = 3;

  @NotBlank
  String image = "public.ecr.aws/dissco/dissco-export-job:latest";

  @NotBlank
  String namespace = "data-export-job";

  @NotBlank
  String bucketName = "dissco-data-export-test";

}

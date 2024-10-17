package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("queue")
public class QueueProperties {

  @Positive
  Integer size = 3;

}

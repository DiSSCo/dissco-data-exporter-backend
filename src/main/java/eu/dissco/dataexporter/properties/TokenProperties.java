package eu.dissco.dataexporter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("token")
public class TokenProperties {

  private String secretName;
  private String idName;

}

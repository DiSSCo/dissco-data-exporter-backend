package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("token")
public class TokenProperties {

  @NotBlank
  private String secretName;
  @NotBlank
  private String idName;

}

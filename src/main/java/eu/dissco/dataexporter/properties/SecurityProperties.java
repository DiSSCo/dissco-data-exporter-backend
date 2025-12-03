package eu.dissco.dataexporter.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NotBlank
@ConfigurationProperties("security")
public class SecurityProperties {

  @NotBlank
  private String clientId = "dissco-data-exporter-backend";

}

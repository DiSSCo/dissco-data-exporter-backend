package eu.dissco.dataexporter.configuration;

import freemarker.template.Template;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TemplateConfiguration {

  private final freemarker.template.Configuration configuration;

  @Bean
  public Template template() throws IOException {
    return configuration.getTemplate("export-job.ftl");
  }

}

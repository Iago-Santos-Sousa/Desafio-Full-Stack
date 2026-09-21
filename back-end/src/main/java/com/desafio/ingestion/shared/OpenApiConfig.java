package com.desafio.ingestion.shared;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI dataPulseOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("DataPulse Ingestion API")
                .version("v1")
                .description(
                    "API para upload e processamento assíncrono de CSVs dinâmicos. "
                        + "O upload é gravado em streaming, processado por Spring Batch "
                        + "e acompanhado por polling."));
  }
}

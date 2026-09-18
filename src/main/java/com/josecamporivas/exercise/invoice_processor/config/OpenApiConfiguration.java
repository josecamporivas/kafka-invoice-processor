package com.josecamporivas.exercise.invoice_processor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

	@Bean
	OpenAPI invoiceProcessorOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Invoice Processor API")
						.description("Kafka-based electronic invoice processing service")
						.version("0.0.1"));
	}
}

package com.josecamporivas.exercise.invoice_processor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@EnableKafka
public class KafkaConfiguration {

	@Bean
	NewTopic incomingInvoicesTopic() {
		return TopicBuilder.name(KafkaTopics.INCOMING_INVOICES)
				.partitions(1)
				.replicas(1)
				.build();
	}

	@Bean
	NewTopic validatedInvoicesTopic() {
		return TopicBuilder.name(KafkaTopics.VALIDATED_INVOICES)
				.partitions(1)
				.replicas(1)
				.build();
	}

	@Bean
	DefaultErrorHandler kafkaErrorHandler() {
		return new DefaultErrorHandler(new FixedBackOff(1000L, 2L));
	}
}

package com.josecamporivas.exercise.invoice_processor.messaging;

import com.josecamporivas.exercise.invoice_processor.config.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ValidatedInvoicePublisher {

	private static final long SEND_TIMEOUT_SECONDS = 10L;

	private final KafkaTemplate<Object, Object> kafkaTemplate;

	public ValidatedInvoicePublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(String invoiceId, String xml) {
		try {
			kafkaTemplate.send(KafkaTopics.VALIDATED_INVOICES, invoiceId, xml)
					.get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new InvoicePublishingException("Publishing invoice was interrupted: " + invoiceId, exception);
		} catch (ExecutionException | TimeoutException exception) {
			throw new InvoicePublishingException("Unable to publish invoice: " + invoiceId, exception);
		}
	}
}

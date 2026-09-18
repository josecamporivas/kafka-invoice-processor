package com.josecamporivas.exercise.invoice_processor.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.josecamporivas.exercise.invoice_processor.config.KafkaTopics;
import com.josecamporivas.exercise.invoice_processor.invoice.Invoice;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoiceService;
import com.josecamporivas.exercise.invoice_processor.processing.InvoiceXmlTransformer;
import com.josecamporivas.exercise.invoice_processor.validation.InvoiceValidationException;
import com.josecamporivas.exercise.invoice_processor.validation.InvoiceValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceKafkaListener {

	private static final Logger log = LoggerFactory.getLogger(InvoiceKafkaListener.class);

	private final ObjectMapper objectMapper;
	private final InvoiceValidationService validationService;
	private final InvoiceXmlTransformer xmlTransformer;
	private final ValidatedInvoicePublisher publisher;
	private final InvalidInvoiceService invalidInvoiceService;

	public InvoiceKafkaListener(
			ObjectMapper objectMapper,
			InvoiceValidationService validationService,
			InvoiceXmlTransformer xmlTransformer,
			ValidatedInvoicePublisher publisher,
			InvalidInvoiceService invalidInvoiceService
	) {
		this.objectMapper = objectMapper;
		this.validationService = validationService;
		this.xmlTransformer = xmlTransformer;
		this.publisher = publisher;
		this.invalidInvoiceService = invalidInvoiceService;
	}

	@KafkaListener(
			topics = KafkaTopics.INCOMING_INVOICES,
			groupId = "${spring.kafka.consumer.group-id:invoice-processor}"
	)
	public void consume(String payload) {
		try {
			if (payload == null || payload.isBlank()) {
				throw new InvoiceValidationException("invoice payload must not be empty");
			}

			Invoice invoice = objectMapper.readValue(payload, Invoice.class);
			validationService.validate(invoice);
			publisher.publish(invoice.invoiceId(), xmlTransformer.transform(invoice));
		} catch (JsonProcessingException exception) {
			String reason = "Invalid JSON: " + exception.getOriginalMessage();
			storeInvalidInvoice(payload, reason);
		} catch (InvoiceValidationException exception) {
			storeInvalidInvoice(payload, exception.getMessage());
		}
	}

	private void storeInvalidInvoice(String payload, String reason) {
		String invoiceId = extractInvoiceId(payload);
		invalidInvoiceService.save(invoiceId, payload, reason);
		log.warn("Rejected invoice {}: {}", invoiceId == null ? "<unknown>" : invoiceId, reason);
	}

	private String extractInvoiceId(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}

		try {
			JsonNode invoiceId = objectMapper.readTree(payload).get("invoiceId");
			return invoiceId == null || !invoiceId.isTextual() ? null : invoiceId.asText();
		} catch (JsonProcessingException exception) {
			return null;
		}
	}
}

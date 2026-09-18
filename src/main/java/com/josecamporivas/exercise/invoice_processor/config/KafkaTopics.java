package com.josecamporivas.exercise.invoice_processor.config;

public final class KafkaTopics {

	public static final String INCOMING_INVOICES = "incoming-invoices";
	public static final String VALIDATED_INVOICES = "validated-invoices";

	private KafkaTopics() {
	}
}

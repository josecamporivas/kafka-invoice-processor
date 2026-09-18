package com.josecamporivas.exercise.invoice_processor.messaging;

public class InvoicePublishingException extends RuntimeException {

	public InvoicePublishingException(String message, Throwable cause) {
		super(message, cause);
	}
}

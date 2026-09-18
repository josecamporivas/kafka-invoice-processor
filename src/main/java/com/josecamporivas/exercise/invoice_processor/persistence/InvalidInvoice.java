package com.josecamporivas.exercise.invoice_processor.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "invalid_invoices")
public class InvalidInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "invoice_id", length = 64)
	private String invoiceId;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String payload;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String reason;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	protected InvalidInvoice() {
	}

	public InvalidInvoice(String invoiceId, String payload, String reason, Instant receivedAt) {
		this.invoiceId = invoiceId;
		this.payload = payload;
		this.reason = reason;
		this.receivedAt = receivedAt;
	}

	public Long getId() {
		return id;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public String getPayload() {
		return payload;
	}

	public String getReason() {
		return reason;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
}

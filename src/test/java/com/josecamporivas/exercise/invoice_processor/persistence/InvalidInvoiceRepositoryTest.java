package com.josecamporivas.exercise.invoice_processor.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InvalidInvoiceRepositoryTest {

	@Autowired
	private InvalidInvoiceRepository repository;

	@Test
	void storesLargePayloadsAndReasons() {
		String payload = "{\"payload\":\"" + "x".repeat(500) + "\"}";
		String reason = "validation failed: " + "y".repeat(500);

		InvalidInvoice saved = repository.saveAndFlush(new InvalidInvoice(
				"INV-INVALID-001",
				payload,
				reason,
				Instant.parse("2024-01-01T00:00:00Z")
		));

		InvalidInvoice loaded = repository.findById(saved.getId()).orElseThrow();

		assertThat(loaded.getInvoiceId()).isEqualTo("INV-INVALID-001");
		assertThat(loaded.getPayload()).isEqualTo(payload);
		assertThat(loaded.getReason()).isEqualTo(reason);
	}
}

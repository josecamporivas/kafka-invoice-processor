package com.josecamporivas.exercise.invoice_processor.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InvalidInvoiceService {

	private final InvalidInvoiceRepository repository;

	public InvalidInvoiceService(InvalidInvoiceRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void save(String invoiceId, String payload, String reason) {
		repository.save(new InvalidInvoice(
				invoiceId,
				payload == null ? "" : payload,
				reason == null || reason.isBlank() ? "Invoice validation failed" : reason,
				Instant.now()
		));
	}

	@Transactional(readOnly = true)
	public Page<InvalidInvoice> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}
}

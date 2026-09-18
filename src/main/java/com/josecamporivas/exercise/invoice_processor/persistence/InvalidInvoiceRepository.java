package com.josecamporivas.exercise.invoice_processor.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidInvoiceRepository extends JpaRepository<InvalidInvoice, Long> {
}

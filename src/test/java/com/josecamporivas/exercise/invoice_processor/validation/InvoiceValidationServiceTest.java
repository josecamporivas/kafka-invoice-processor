package com.josecamporivas.exercise.invoice_processor.validation;

import com.josecamporivas.exercise.invoice_processor.invoice.Invoice;
import com.josecamporivas.exercise.invoice_processor.invoice.InvoiceLine;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class InvoiceValidationServiceTest {

	private ValidatorFactory validatorFactory;
	private InvoiceValidationService validationService;

	@BeforeEach
	void setUp() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		validationService = new InvoiceValidationService(validator);
	}

	@AfterEach
	void tearDown() {
		validatorFactory.close();
	}

	@Test
	void acceptsAValidGermanInvoice() {
		assertThatCode(() -> validationService.validate(invoice(
				"12345678A",
				"DE",
				new BigDecimal("1000"),
				new BigDecimal("0.19")
		))).doesNotThrowAnyException();
	}

	@Test
	void rejectsAnUnknownCountry() {
		assertThatThrownBy(() -> validationService.validate(invoice(
				"12345678A",
				"ZZ",
				new BigDecimal("1000"),
				new BigDecimal("0.19")
		)))
				.isInstanceOf(InvoiceValidationException.class)
				.hasMessageContaining("ISO 3166-1 alpha-2");
	}

	@Test
	void rejectsAnInvalidCustomerIdFormatForSpain() {
		assertThatThrownBy(() -> validationService.validate(invoice(
				"123",
				"ES",
				new BigDecimal("1000"),
				new BigDecimal("0.19")
		)))
				.isInstanceOf(InvoiceValidationException.class)
				.hasMessageContaining("customerId");
	}

	@Test
	void rejectsNonPositiveLineAmounts() {
		assertThatThrownBy(() -> validationService.validate(invoice(
				"12345678A",
				"DE",
				BigDecimal.ZERO,
				new BigDecimal("0.19")
		)))
				.isInstanceOf(InvoiceValidationException.class)
				.hasMessageContaining("amount must be greater than zero");
	}

	private Invoice invoice(String customerId, String country, BigDecimal amount, BigDecimal vatRate) {
		return new Invoice(
				"INV-20240101-001",
				customerId,
				country,
				LocalDate.of(2024, 1, 1),
				List.of(new InvoiceLine("Laptop", amount, vatRate))
		);
	}
}

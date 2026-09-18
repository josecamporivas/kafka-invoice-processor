package com.josecamporivas.exercise.invoice_processor.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record Invoice(
		@NotBlank
		@Size(max = 64)
		String invoiceId,

		@NotBlank
		@Size(max = 64)
		String customerId,

		@NotBlank(message = "country is required")
		@Pattern(regexp = "[A-Za-z]{2}", message = "country must be a two-letter ISO code")
		String country,

		@NotNull
		LocalDate issueDate,

		@NotEmpty
		@Valid
		List<InvoiceLine> lines
) {
}

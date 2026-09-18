package com.josecamporivas.exercise.invoice_processor.invoice;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InvoiceLine(
		@NotBlank
		String description,

		@NotNull
		@DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero")
		BigDecimal amount,

		@NotNull
		@DecimalMin(value = "0.0", message = "vatRate cannot be negative")
		@DecimalMax(value = "1.0", message = "vatRate cannot be greater than 1")
		BigDecimal vatRate
) {
}

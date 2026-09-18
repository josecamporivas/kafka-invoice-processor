package com.josecamporivas.exercise.invoice_processor.validation;

import com.josecamporivas.exercise.invoice_processor.invoice.Invoice;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InvoiceValidationService {

	private static final Set<String> ISO_COUNTRIES =
			Collections.unmodifiableSet(Set.copyOf(Arrays.asList(Locale.getISOCountries())));

	private static final Map<String, Pattern> CUSTOMER_ID_PATTERNS = Map.of(
			"DE", Pattern.compile("(?:DE)?[A-Z0-9]{9,14}"),
			"ES", Pattern.compile("(?:[0-9]{8}[A-Z]|[XYZ][0-9]{7}[A-Z])"),
			"FR", Pattern.compile("(?:FR)?[A-Z0-9]{2}[0-9]{9}")
	);
	private static final Pattern DEFAULT_CUSTOMER_ID_PATTERN = Pattern.compile("[A-Z0-9][A-Z0-9-]{2,31}");

	private final Validator validator;

	public InvoiceValidationService(Validator validator) {
		this.validator = validator;
	}

	public void validate(Invoice invoice) {
		if (invoice == null) {
			throw new InvoiceValidationException("invoice payload must not be null");
		}

		String beanViolations = validator.validate(invoice).stream()
				.map(this::formatViolation)
				.sorted()
				.collect(Collectors.joining("; "));
		if (!beanViolations.isBlank()) {
			throw new InvoiceValidationException(beanViolations);
		}

		String country = invoice.country().trim().toUpperCase(Locale.ROOT);
		if (!ISO_COUNTRIES.contains(country)) {
			throw new InvoiceValidationException("country is not a valid ISO 3166-1 alpha-2 code: " + invoice.country());
		}

		String customerId = invoice.customerId().trim().toUpperCase(Locale.ROOT);
		Pattern customerIdPattern = CUSTOMER_ID_PATTERNS.getOrDefault(
				country,
				DEFAULT_CUSTOMER_ID_PATTERN
		);
		if (!customerIdPattern.matcher(customerId).matches()) {
			throw new InvoiceValidationException("customerId does not match the expected format for country " + country);
		}
	}

	private String formatViolation(ConstraintViolation<Invoice> violation) {
		return violation.getPropertyPath() + ": " + violation.getMessage();
	}
}

package com.josecamporivas.exercise.invoice_processor.web;

import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoice;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvalidInvoiceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InvalidInvoiceRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void returnsRequestedPageSortedByReceivedAtDescending() throws Exception {
		seedThreeInvalidInvoices();

		mockMvc.perform(get("/api/v1/invalid-invoices")
						.param("page", "0")
						.param("size", "2")
						.param("sort", "receivedAt,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[0].invoiceId").value("INV-003"))
				.andExpect(jsonPath("$.content[1].invoiceId").value("INV-002"))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.numberOfElements").value(2))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(false))
				.andExpect(jsonPath("$.empty").value(false))
				.andExpect(jsonPath("$.content[0].payload").doesNotExist());
	}

	@Test
	void returnsLastPage() throws Exception {
		seedThreeInvalidInvoices();

		mockMvc.perform(get("/api/v1/invalid-invoices")
						.param("page", "1")
						.param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].invoiceId").value("INV-001"))
				.andExpect(jsonPath("$.number").value(1))
				.andExpect(jsonPath("$.numberOfElements").value(1))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.first").value(false))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	void defaultsToReceivedAtDescendingWhenNoSortProvided() throws Exception {
		seedThreeInvalidInvoices();

		mockMvc.perform(get("/api/v1/invalid-invoices"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].invoiceId").value("INV-003"))
				.andExpect(jsonPath("$.content[1].invoiceId").value("INV-002"))
				.andExpect(jsonPath("$.content[2].invoiceId").value("INV-001"))
				.andExpect(jsonPath("$.totalElements").value(3));
	}

	private void seedThreeInvalidInvoices() {
		repository.saveAll(List.of(
				invalidInvoice("INV-001", Instant.parse("2024-01-01T10:00:00Z")),
				invalidInvoice("INV-002", Instant.parse("2024-01-02T10:00:00Z")),
				invalidInvoice("INV-003", Instant.parse("2024-01-03T10:00:00Z"))
		));
	}

	private InvalidInvoice invalidInvoice(String invoiceId, Instant receivedAt) {
		return new InvalidInvoice(
				invoiceId,
				"{\"invoiceId\":\"" + invoiceId + "\"}",
				"amount must be greater than zero",
				receivedAt
		);
	}
}

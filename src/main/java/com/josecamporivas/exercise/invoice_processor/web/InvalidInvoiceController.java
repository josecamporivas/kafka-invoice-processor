package com.josecamporivas.exercise.invoice_processor.web;

import com.josecamporivas.exercise.invoice_processor.api.InvalidInvoicesApi;
import com.josecamporivas.exercise.invoice_processor.api.model.InvalidInvoicePage;
import com.josecamporivas.exercise.invoice_processor.api.model.InvalidInvoiceResponse;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoice;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.List;

@RestController
public class InvalidInvoiceController implements InvalidInvoicesApi {

	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "receivedAt");

	private final InvalidInvoiceService invalidInvoiceService;

	public InvalidInvoiceController(InvalidInvoiceService invalidInvoiceService) {
		this.invalidInvoiceService = invalidInvoiceService;
	}

	@Override
	public ResponseEntity<InvalidInvoicePage> listInvalidInvoices(Pageable pageable) {
		Pageable effectivePageable = pageable.getSort().isSorted()
				? pageable
				: PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

		Page<InvalidInvoice> page = invalidInvoiceService.findAll(effectivePageable);

		return ResponseEntity.ok(toPageResponse(page));
	}

	private InvalidInvoicePage toPageResponse(Page<InvalidInvoice> page) {
		List<InvalidInvoiceResponse> content = page.getContent().stream()
				.map(this::toResponse)
				.toList();

		return new InvalidInvoicePage(
				content,
				page.getNumber(),
				page.getSize(),
				page.getNumberOfElements(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast(),
				page.isEmpty()
		);
	}

	private InvalidInvoiceResponse toResponse(InvalidInvoice invalidInvoice) {
		return new InvalidInvoiceResponse(
				invalidInvoice.getId(),
				invalidInvoice.getReason(),
				invalidInvoice.getReceivedAt().atOffset(ZoneOffset.UTC)
		).invoiceId(invalidInvoice.getInvoiceId());
	}
}

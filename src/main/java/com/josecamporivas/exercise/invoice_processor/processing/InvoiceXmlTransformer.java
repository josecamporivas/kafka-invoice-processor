package com.josecamporivas.exercise.invoice_processor.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.josecamporivas.exercise.invoice_processor.invoice.Invoice;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
public class InvoiceXmlTransformer {

	private final XmlMapper xmlMapper;

	public InvoiceXmlTransformer() {
		this.xmlMapper = XmlMapper.builder().build();
	}

	public String transform(Invoice invoice) {
		BigDecimal total = invoice.lines().stream()
				.map(line -> line.amount().multiply(BigDecimal.ONE.add(line.vatRate())))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		ValidatedInvoiceXml xmlInvoice = new ValidatedInvoiceXml(
				invoice.invoiceId(),
				invoice.country().trim().toUpperCase(Locale.ROOT),
				total
		);

		try {
			return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlInvoice);
		} catch (JsonProcessingException exception) {
			throw new InvoiceTransformationException("Unable to transform invoice to XML", exception);
		}
	}
}

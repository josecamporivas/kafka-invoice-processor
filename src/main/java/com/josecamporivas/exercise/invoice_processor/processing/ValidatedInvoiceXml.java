package com.josecamporivas.exercise.invoice_processor.processing;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;

@JacksonXmlRootElement(localName = "Invoice")
public record ValidatedInvoiceXml(
		@JacksonXmlProperty(localName = "Id") String id,
		@JacksonXmlProperty(localName = "Country") String country,
		@JacksonXmlProperty(localName = "Total") BigDecimal total
) {
}

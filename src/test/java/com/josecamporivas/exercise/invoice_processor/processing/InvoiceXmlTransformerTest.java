package com.josecamporivas.exercise.invoice_processor.processing;

import com.josecamporivas.exercise.invoice_processor.invoice.Invoice;
import com.josecamporivas.exercise.invoice_processor.invoice.InvoiceLine;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceXmlTransformerTest {

	private final InvoiceXmlTransformer transformer = new InvoiceXmlTransformer();

	@Test
	void transformsInvoiceAndCalculatesVatInclusiveTotal() throws Exception {
		Invoice invoice = new Invoice(
				"INV-20240101-001",
				"12345678A",
				"de",
				LocalDate.of(2024, 1, 1),
				List.of(
						new InvoiceLine("Laptop", new BigDecimal("1000"), new BigDecimal("0.19")),
						new InvoiceLine("Mouse", new BigDecimal("50"), new BigDecimal("0.20"))
				)
		);

		String xml = transformer.transform(invoice);
		Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(new InputSource(new StringReader(xml)));

		assertThat(document.getDocumentElement().getNodeName()).isEqualTo("Invoice");
		assertThat(text(document, "Id")).isEqualTo("INV-20240101-001");
		assertThat(text(document, "Country")).isEqualTo("DE");
		assertThat(text(document, "Total")).isEqualTo("1250.00");
	}

	private String text(Document document, String elementName) {
		return document.getElementsByTagName(elementName).item(0).getTextContent();
	}
}

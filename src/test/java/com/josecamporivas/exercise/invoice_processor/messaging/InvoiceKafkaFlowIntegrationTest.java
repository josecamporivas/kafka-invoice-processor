package com.josecamporivas.exercise.invoice_processor.messaging;

import com.josecamporivas.exercise.invoice_processor.config.KafkaTopics;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoice;
import com.josecamporivas.exercise.invoice_processor.persistence.InvalidInvoiceRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
		"spring.kafka.admin.auto-create=true",
		"spring.kafka.consumer.group-id=invoice-flow-test",
		"spring.kafka.listener.auto-startup=true"
})
@EmbeddedKafka(
		partitions = 1,
		topics = {KafkaTopics.INCOMING_INVOICES, KafkaTopics.VALIDATED_INVOICES},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class InvoiceKafkaFlowIntegrationTest {

	@Autowired
	private KafkaTemplate<Object, Object> kafkaTemplate;

	@Autowired
	private InvalidInvoiceRepository invalidInvoiceRepository;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	private Consumer<String, String> outputConsumer;

	@BeforeEach
	void setUp() {
		invalidInvoiceRepository.deleteAll();

		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				"validated-output-test",
				"false",
				embeddedKafka
		);
		consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		outputConsumer = new DefaultKafkaConsumerFactory<>(
				consumerProperties,
				new StringDeserializer(),
				new StringDeserializer()
		).createConsumer();
		embeddedKafka.consumeFromAnEmbeddedTopic(outputConsumer, KafkaTopics.VALIDATED_INVOICES);
	}

	@AfterEach
	void tearDown() {
		if (outputConsumer != null) {
			outputConsumer.close(Duration.ofSeconds(5));
		}
	}

	@Test
	void consumesValidInvoiceAndPublishesXml() throws Exception {
		String invoiceId = "INV-INTEGRATION-001";
		kafkaTemplate.send(KafkaTopics.INCOMING_INVOICES, invoiceId, validInvoice(invoiceId)).get(10, TimeUnit.SECONDS);

		ConsumerRecord<String, String> output = KafkaTestUtils.getSingleRecord(
				outputConsumer,
				KafkaTopics.VALIDATED_INVOICES,
				Duration.ofSeconds(10)
		);

		assertThat(output.key()).isEqualTo(invoiceId);
		assertThat(output.value())
				.contains("<Id>" + invoiceId + "</Id>")
				.contains("<Country>DE</Country>")
				.contains("<Total>1190.00</Total>");
		assertThat(invalidInvoiceRepository.count()).isZero();
	}

	@Test
	void storesValidationFailuresWithoutPublishing() throws Exception {
		String invoiceId = "INV-INTEGRATION-INVALID";
		String payload = validInvoice(invoiceId).replace("\"amount\":1000", "\"amount\":0");

		kafkaTemplate.send(KafkaTopics.INCOMING_INVOICES, invoiceId, payload).get(10, TimeUnit.SECONDS);

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			List<InvalidInvoice> invalidInvoices = invalidInvoiceRepository.findAll();
			assertThat(invalidInvoices).hasSize(1);
			assertThat(invalidInvoices.get(0).getInvoiceId()).isEqualTo(invoiceId);
			assertThat(invalidInvoices.get(0).getReason()).contains("amount must be greater than zero");
		});
	}

	@Test
	void storesMalformedJson() throws Exception {
		kafkaTemplate.send(KafkaTopics.INCOMING_INVOICES, "invalid-json", "{not-json").get(10, TimeUnit.SECONDS);

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			List<InvalidInvoice> invalidInvoices = invalidInvoiceRepository.findAll();
			assertThat(invalidInvoices).hasSize(1);
			assertThat(invalidInvoices.get(0).getInvoiceId()).isNull();
			assertThat(invalidInvoices.get(0).getReason()).contains("Invalid JSON");
		});
	}

	private String validInvoice(String invoiceId) {
		return "{"
				+ "\"invoiceId\":\"" + invoiceId + "\","
				+ "\"customerId\":\"12345678A\","
				+ "\"country\":\"DE\","
				+ "\"issueDate\":\"2024-01-01\","
				+ "\"lines\":[{\"description\":\"Laptop\",\"amount\":1000,\"vatRate\":0.19}]"
				+ "}";
	}
}

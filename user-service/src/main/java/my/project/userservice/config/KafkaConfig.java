package my.project.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class KafkaConfig {

	public static final String RETRY_KAFKA_TEMPLATE = "retryKafkaTemplate";

	public static final String DLT_SUFFIX = "-dlt";

	private static final int CONSUMER_CONCURRENCY = 3;

	private static final Duration DLT_RETENTION = Duration.ofDays(30);

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {

		log.info("Инициализация KafkaListenerContainerFactory для user-service");

		var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
		factory.setConsumerFactory(consumerFactory);
		factory.setConcurrency(CONSUMER_CONCURRENCY);
		factory.getContainerProperties().setObservationEnabled(true);
		return factory;
	}

	@Bean(RETRY_KAFKA_TEMPLATE)
	public KafkaTemplate<String, Object> retryKafkaTemplate(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

		log.info("Инициализация KafkaTemplate для retry- и DLT-топиков");

		var jsonSerializer = new JacksonJsonSerializer<>();
		jsonSerializer.setAddTypeInfo(false);

		Map<Class<?>, Serializer<?>> valueSerializers = new LinkedHashMap<>();
		valueSerializers.put(byte[].class, new ByteArraySerializer());
		valueSerializers.put(Object.class, jsonSerializer);

		var producerFactory = new DefaultKafkaProducerFactory<>(
				Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
				new StringSerializer(),
				new DelegatingByTypeSerializer(valueSerializers, true));

		var template = new KafkaTemplate<>(producerFactory);
		template.setObservationEnabled(true);
		return template;
	}

	@Bean
	public ApplicationRunner deadLetterTopicsRetention(KafkaAdmin kafkaAdmin,
													   @Value("${app.kafka.topics.manager-restaurants-changed}") String managerRestaurantsChangedTopic) {
		return args -> {
			var retention = new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(DLT_RETENTION.toMillis()));
			Map<ConfigResource, Collection<AlterConfigOp>> changes = new LinkedHashMap<>();
			for (String topic : List.of(managerRestaurantsChangedTopic)) {
				changes.put(new ConfigResource(ConfigResource.Type.TOPIC, topic + DLT_SUFFIX),
						List.of(new AlterConfigOp(retention, AlterConfigOp.OpType.SET)));
			}

			try (var adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
				adminClient.incrementalAlterConfigs(changes).all().get(10, TimeUnit.SECONDS);
				log.info("Срок хранения DLT-топиков установлен: {} дней, topics={}", DLT_RETENTION.toDays(), changes.keySet());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				log.warn("Не удалось установить срок хранения DLT-топиков, применится при следующем запуске", e);
			}
		};
	}
}

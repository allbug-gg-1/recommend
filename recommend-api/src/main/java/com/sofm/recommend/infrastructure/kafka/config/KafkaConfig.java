package com.sofm.recommend.infrastructure.kafka.config;

import com.sofm.recommend.infrastructure.kafka.consumer.KafkaMessageHandler;
import com.sofm.recommend.infrastructure.kafka.streams.KafkaStreamsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    // 从配置文件中读取源 Redis 配置信息
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;



    private final KafkaMessageHandler kafkaMessageHandler;
    private final KafkaStreamsService kafkaStreamsService;


    public KafkaConfig(KafkaMessageHandler kafkaMessageHandler, KafkaStreamsService kafkaStreamsService) {
        this.kafkaMessageHandler = kafkaMessageHandler;
        this.kafkaStreamsService = kafkaStreamsService;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 配置 Kafka 消息监听容器
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler((exception, data) -> {
            log.error("Error processing Kafka message: {}", data, exception);
        }));
        return factory;
    }


    // 手动创建并管理消息监听容器
    @Bean
    public ConcurrentMessageListenerContainer<String, String> messageListenerContainer() {
        // 配置ContainerProperties, 指定Kafka主题
        ContainerProperties containerProps = new ContainerProperties("ugc");
        // 设置消息监听器，处理收到的消息
        containerProps.setMessageListener(kafkaMessageHandler);
        return new ConcurrentMessageListenerContainer<>(consumerFactory(), containerProps);
    }


    @Bean
    public Properties kafkaStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        props.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 1000); // 增加每个分区的缓冲区大小
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100); // 减少 commit 间隔，尽快提交已处理的记录
        return props;
    }

    @Bean
    public StreamsBuilder streamsBuilder() {
        return new StreamsBuilder();
    }


    @Bean
    public KafkaStreams kafkaStreams(StreamsBuilder builder, Properties kafkaStreamsConfig) {
        // 调用 StreamsService 中的方法来定义具体的业务逻辑
        kafkaStreamsService.createHeatAggregationTopology(builder);
        // 创建并启动 Kafka Streams 实例
        Topology topology = builder.build();
        KafkaStreams kafkaStreams = new KafkaStreams(topology, kafkaStreamsConfig);
        kafkaStreams.start();
        return kafkaStreams;
    }

}

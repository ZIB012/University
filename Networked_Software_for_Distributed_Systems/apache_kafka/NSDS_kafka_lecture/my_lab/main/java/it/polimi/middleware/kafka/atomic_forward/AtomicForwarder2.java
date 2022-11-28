package it.polimi.middleware.kafka.atomic_forward;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

// it's both a consumer and a producer:
    // it reads from topicA (the topic of the original producer) and puts all the messages inside the topicB
// we want to have exactly 1 guarantees --> even if this forwarder crashes, it restarts from where it left
public class AtomicForwarder2 {
    private static final String defaultConsumerGroupId = "groupA";
    private static final String defaultInputTopic = "topicA";
    private static final String defaultOutputTopic = "new_topic";

    private static final String serverAddr = "localhost:9092";
    private static final String producerTransactionalId = "forwarderTransactionalId";

    public static void main(String[] args) {
        // If there are arguments, use the first as group, the second as input topic, the third as output topic.
        // Otherwise, use default group and topics.
        String consumerGroupId = args.length >= 1 ? args[0] : defaultConsumerGroupId;
        String inputTopic = args.length >= 2 ? args[1] : defaultInputTopic;
        String outputTopic = args.length >= 3 ? args[2] : defaultOutputTopic;

        // We need to instantiate both a Kafka consumer and a Kafka producer:
        // Consumer
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // to make sure that it reads messages that are pushed and commited
        // The consumer does not commit automatically, but within the producer transaction
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false)); //we don't want to commit
            // offsets periodically but we want to commit this offsets as part of the same transaction in which we write topicB

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // Producer
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
        // producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
        // the producer will initiate the transaction: every time we read a new message from topicA, the producer will
        // start a new transaction, within this transaction the producer will write the record to topicB; then the
        // producer will manually commit all the offsets for the consumer within the same transaction (it will update
        // the offset of the messages that have been read as part of the same transaction that writes to the output topic)
        // to do that, it needs to create a data structure that contains the latest offset for each partition of the topic,
        // and then it can use the method "producer.sendOffsetsToTransacion()" to commit as part of this transaction
        // these offsets, and then it commits the transaction
        // producer.initTransactions();

        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            // producer.beginTransaction();
            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                String output = record.value();
                output = output.toLowerCase();
                producer.send(new ProducerRecord<>(outputTopic, record.key(), output));
            }

            // The producer manually commits the offsets for the consumer within the transaction
            /*
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }

            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();
            */
        }
    }
}
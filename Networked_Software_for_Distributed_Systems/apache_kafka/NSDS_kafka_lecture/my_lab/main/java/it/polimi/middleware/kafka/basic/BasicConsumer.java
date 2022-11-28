package it.polimi.middleware.kafka.basic;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

public class BasicConsumer {
    private static final String defaultGroupId = "groupA";
    private static final String defaultTopic = "topicA";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        String topic = args.length >= 2 ? args[1] : defaultTopic;

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr); // list of servers the consumer connects to
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // unique identifier of the consumer group the consumer belongs to
        // if 2 consumers belong to the same consumer group and subscribe to the same topic, then messages are submitted
        // either to one consumer or to the other

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit)); // it refers to the commit of offsets
        // autoCommit --> it means that the offsets of the messages that are read are not updated every time we have read
        // a new message but they are uptated periodically where the period is expressed by the configuration parameter autoCommitIntervalMs
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
            // what happens when there is a new logical consumer a new consumer group, that start from screcth. The two options are "latest" or "earliest"
            // earliest --> new consumer group should receive all the messages that are stored on kafka for the topics they are interested in
            // latest --> it returns only the messages that have been added only after the first consumer.poll()

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());    // default Deserializer for keys
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());  // default Deserializer for values

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);    // to instantiate the kafka consumer we need
            // to instantiate an object of the class KafkaConsumer which is generated over the type of keys and values it consumes
            // the constructor takes as input a properties object that contains configuration parameters

        // Here the consumer subscribes to a list of topics (in this case we subscribe to a single topic which by default is topicA
        consumer.subscribe(Collections.singletonList(topic));
        while (true) {
            // the consumer can poll new messages using the consumer.poll() method, this is a blocking method
            // which remains blocked until there are new records in the list of topics or for a maximum duration
            // so this method returns whene there is a new message or after a maximum duration that here is of 5 minutes
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            // if we receive some records we iterate over them and each record contains information about its key, its value,
            // the partition it's stored on, its offset in that partition
            for (final ConsumerRecord<String, String> record : records) {
                System.out.print("Consumer group: " + groupId + "\t");
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
            }
        }
    }
}
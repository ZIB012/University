package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AverageConsumer2 {

    private static final String serverAddr = "localhost:9092";
    private static final String inputTopic = "inputTopicc";

    // TODO add attributes here if needed
    private static final String offsetResetStrategy = "earliest";

    private static final boolean autoCommit = true;

    public static void main(String[] args) {

        if(args.length == 0)
        {
            System.err.println("You didn't passed the consumer group as an argument");
        }
        final String consumerGroupId = "gruppoB";

        // TODO implement here
        final String InputTopic = args.length >= 2 ? args[1] : inputTopic;

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(InputTopic));

        final Map<String, Integer>  ValueMap = new HashMap<>();
        double sum = 0.0;
        double n = 0.0;
        double local_average = 0.0;
        double average = 0.0;
        while(true)
        {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            for (final ConsumerRecord<String, Integer> record : records)
            {
                if(ValueMap.containsKey(record.key()))
                {
                    sum -= ValueMap.get(record.key());
                    ValueMap.replace(record.key(), record.value());
                    sum += record.value();
                }
                else
                {
                    ValueMap.put(record.key(), record.value());
                    sum += record.value();
                    n++;
                }
                local_average = sum/n;
                if (local_average != average)
                {
                    average = local_average;
                    System.out.println("New average = " + average + " partition:" + record.partition());
                }
            }

        }
    }
}
package it.polimi.middleware.kafka.basic;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class BasicProducer {
    private static final String defaultTopic = "topicA";  // by default we consider only one topic

    private static final int numMessages = 100000;  // given number of messages on which we are iterating
    private static final int waitBetweenMsgs = 500;
    private static final boolean waitAck = true;

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);  // we connect to one broker only (localhost:9092)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); // serializer for keys
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); // serializer for values

        // https://kafka.apache.org/documentation/#producerconfigs we can find a full list of all the possible parameters
        // we can use to configure our kafka producer

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props); // to instantiate a producer we need
            // to create an object of the class kafkaProducer which is generic over the type of the key and value
            // we need to pass also a properties object that contains all the configuration properties
            // in this case we modified only 3 properties which are the state of observer the producer connects to;
            // the serializer for keys and the serializer for values
        final Random r = new Random();

        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));  // it selects one random topic from a list of topics
            final String key = "Key" + r.nextInt(10); // it creates a random key
            final String value = "Val" + i; // it creates a sequential value
            System.out.println(
                    "Topic: " + topic +
                    "\tKey: " + key +
                    "\tValue: " + value
            );

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);  // it creates a record containing the key,
                                                                                    // the value and should be submitted to the selected topic
            final Future<RecordMetadata> future = producer.send(record);    // using the producer.send() method to send the record to kafka
            // this method return a Future containing the MetaData of the record (= informations such as partition and offset)

            if (waitAck) {
                try {
                    RecordMetadata ack = future.get();
                    System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}
# Evaluation lab - Apache Kafka

## Group number: 34

## Group members

- Student 1 Rapetti Marco 10612623
- Student 2 Brenzone Viola 10628532
- Student 3 Lanteri Federico 10677037

## Exercise 1

- Number of partitions allowed for TopicA (1, N)
- Number of consumers allowed (1, M)
    - Consumer 1: <consumerGroupId, (InputTopic, OutputTopic)>
    - Consumer 2: <consumerGroupId, (InputTopic, OutputTopic)>
    - ...
    - Consumer M: <consumerGroupId, (InputTopic, OutputTopic)>

    Where (InputTopic, OutputTopic) are optional arguments, if they are not
      passed through the command line they are assigned by default.

    If you want to increase the performance of the code, more than one consumer
      should have the same consumerGroupId. In that case, the maximum
      possible number of consumers with the same groupId must be n <= N.

## Exercise 2

- Number of partitions allowed for TopicA (1, N)
- Number of consumers allowed (1, M)
    - Consumer 1: <consumerGroupId, (InputTopic)>
    - Consumer 2: <consumerGroupId, (InputTopic)>
    - ...
    - Consumer M: <consumerGroupId, (InputTopic)>

    Where (InputTopic) is an optional argument, if it's not
      passed through the command line it's assigned by default.

    In this case, for the average to be correct, the M consumers must have
      M different groupId, so it has no sense to partition our topic.

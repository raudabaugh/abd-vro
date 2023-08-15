package gov.va.vro.services.bie.service.kafka;

import gov.va.vro.services.bie.config.BieProperties;
import gov.va.vro.services.bie.service.AmqpMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaConsumer {
  private final AmqpMessageSender amqpMessageSender;
  private final BieProperties bieProperties;

  @KafkaListener(
      topics = {
        "#{'${kafka.topic.prefix}'}_CONTENTION_BIE_CONTENTION_ASSOCIATED_TO_CLAIM_V02",
        "#{'${kafka.topic.prefix}'}_CONTENTION_BIE_CONTENTION_UPDATED_V02",
        "#{'${kafka.topic.prefix}'}_CONTENTION_BIE_CONTENTION_CLASSIFIED_V02",
        "#{'${kafka.topic.prefix}'}_CONTENTION_BIE_CONTENTION_COMPLETED_V02",
        "#{'${kafka.topic.prefix}'}_CONTENTION_BIE_CONTENTION_DELETED_V02"
      })
  public void consume(ConsumerRecord<String, Object> record) {
    String messageValue = null;
    String topicName = record.topic();

    if (record.value() instanceof GenericRecord value) {
      messageValue = value.toString();
    } else if (record.value() instanceof String stringValue) {
      messageValue = stringValue;
    }

    log.info("Topic name: {}", topicName);
    log.info("Consumed message key: {}", record.key());
    // TODO: Ensure no PII values are logged
    log.info("Consumed message value (before) decode: {}", messageValue);

    amqpMessageSender.send(
        bieProperties.getKafkaTopicToAmqpExchangeMap().get(topicName), topicName, messageValue);
  }
}
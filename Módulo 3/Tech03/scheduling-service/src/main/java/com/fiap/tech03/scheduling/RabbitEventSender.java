package com.fiap.tech03.scheduling;

import com.fiap.tech03.contracts.RabbitTopology;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventSender implements EventSender {
  private final RabbitTemplate rabbit;

  public RabbitEventSender(RabbitTemplate rabbit) {

    this.rabbit = rabbit;
  }

  public void send(String eventId, String payload) throws Exception {
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    properties.setMessageId(eventId);
    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
    CorrelationData correlation = new CorrelationData(eventId);
    rabbit.send(
        RabbitTopology.EXCHANGE,
        RabbitTopology.KEY,
        new Message(payload.getBytes(StandardCharsets.UTF_8), properties),
        correlation);
    var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
    if (!confirm.ack() || correlation.getReturned() != null)
      throw new IllegalStateException("Broker did not confirm routing");
  }
}

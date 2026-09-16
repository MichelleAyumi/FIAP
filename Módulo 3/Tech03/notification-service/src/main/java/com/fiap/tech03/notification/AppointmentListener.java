package com.fiap.tech03.notification;

import com.fiap.tech03.contracts.*;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentListener {
  private final NotificationInbox inbox;

  public AppointmentListener(NotificationInbox inbox) {

    this.inbox = inbox;
  }

  @RabbitListener(queues = RabbitTopology.QUEUE)
  public void receive(Message message) {
    AppointmentEvent event;
    try {
      event = EventCodec.decode(new String(message.getBody(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new AmqpRejectAndDontRequeueException("Invalid event contract");
    }
    inbox.accept(event);
  }
}

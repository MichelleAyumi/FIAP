package com.fiap.tech03.contracts;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopology {
  public static final String EXCHANGE = "hospital.appointments.v1";
  public static final String KEY = "appointment.changed";
  public static final String QUEUE = "hospital.notifications.v1";
  public static final String DLX = "hospital.notifications.dlx";
  public static final String DLQ = "hospital.notifications.dlq";

  @Bean
  Declarables appointmentTopology() {
    DirectExchange exchange = new DirectExchange(EXCHANGE, true, false);
    DirectExchange dlx = new DirectExchange(DLX, true, false);
    Queue queue =
        QueueBuilder.durable(QUEUE).deadLetterExchange(DLX).deadLetterRoutingKey("failed").build();
    Queue dead = QueueBuilder.durable(DLQ).build();
    return new Declarables(
        exchange,
        dlx,
        queue,
        dead,
        BindingBuilder.bind(queue).to(exchange).with(KEY),
        BindingBuilder.bind(dead).to(dlx).with("failed"));
  }
}

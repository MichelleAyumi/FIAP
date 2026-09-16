package com.fiap.tech03.notification;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageFailureConfig {
  @Bean
  MessageRecoverer messageRecoverer() {
    return (message, cause) -> {

      throw new AmqpRejectAndDontRequeueException("Message processing exhausted; routed to DLQ");
    };
  }
}

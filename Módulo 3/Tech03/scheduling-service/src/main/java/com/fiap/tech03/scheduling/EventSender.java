package com.fiap.tech03.scheduling;

public interface EventSender {
  void send(String eventId, String payload) throws Exception;
}

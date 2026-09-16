package com.fiap.tech03.contracts;

import tools.jackson.databind.json.JsonMapper;

public final class EventCodec {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private EventCodec() {

  }

  public static String encode(AppointmentEvent event) {

    return JSON.writeValueAsString(event);
  }

  public static AppointmentEvent decode(String json) {
    AppointmentEvent event = JSON.readValue(json, AppointmentEvent.class);
    event.validate();
    return event;
  }
}

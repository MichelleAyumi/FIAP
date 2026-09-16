package com.fiap.tech03.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiap.tech03.contracts.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:notificationtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "app.jobs.enabled=false",
      "spring.rabbitmq.dynamic=false",
      "spring.rabbitmq.listener.simple.auto-startup=false"
    })
@ActiveProfiles("demo")
class NotificationIntegrationTest {
  @Autowired NotificationInbox inbox;
  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager tx;
  @Autowired AppointmentListener listener;
  @Autowired WebApplicationContext context;

  @Test
  void monitoringAcceptsOnlyValidCredentials() throws Exception {
    var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    mvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
    mvc.perform(get("/actuator/prometheus").with(httpBasic("monitor", "wrong")))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/actuator/prometheus").with(httpBasic("monitor", "Monitor123!")))
        .andExpect(status().isOk());
  }

  @BeforeEach
  void clear() {
    jdbc.update("DELETE FROM notification_jobs");
    jdbc.update("DELETE FROM appointment_snapshots");
    jdbc.update("DELETE FROM inbox_events");
  }

  private AppointmentEvent event(long version, Instant startsAt) {
    return new AppointmentEvent(
        UUID.randomUUID().toString(),
        version == 1 ? "APPOINTMENT_CREATED" : "APPOINTMENT_UPDATED",
        1,
        "appointment-1",
        version,
        "pac-001",
        "patient@example.test",
        "doc-001",
        startsAt.toString(),
        Instant.now().toString());
  }

  private ReminderDispatcher dispatcher(JavaMailSender mail, Instant now) {
    return new ReminderDispatcher(
        jdbc,
        mail,
        Clock.fixed(now, ZoneOffset.UTC),
        tx,
        new SimpleMeterRegistry(),
        "hospital@example.test");
  }

  @Test
  void duplicateAndStaleEventsDoNotCreateDuplicateJobs() {
    var first = event(2, Instant.now().plusSeconds(172800));
    inbox.accept(first);
    inbox.accept(first);
    inbox.accept(event(1, Instant.now().plusSeconds(180000)));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notification_jobs", Integer.class))
        .isEqualTo(2);
    assertThat(jdbc.queryForObject("SELECT version FROM appointment_snapshots", Long.class))
        .isEqualTo(2);
  }

  @Test
  void reschedulingCancelsOldReminderAndUsesNewTime() {
    Instant now = Instant.now();
    inbox.accept(event(1, now.plusSeconds(172800)));
    inbox.accept(event(2, now.plusSeconds(259200)));
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM notification_jobs WHERE status='CANCELLED'", Integer.class))
        .isEqualTo(2);
    JavaMailSender mail = mock(JavaMailSender.class);
    dispatcher(mail, now.plusSeconds(90000)).dispatch();
    verify(mail, times(1)).send(any(SimpleMailMessage.class));
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM notification_jobs WHERE status='PENDING' AND kind='REMINDER'",
                Integer.class))
        .isEqualTo(1);
  }

  @Test
  void sendsConfirmationAndLaterReminderOnceEach() {
    Instant now = Instant.now();
    inbox.accept(event(1, now.plusSeconds(172800)));
    JavaMailSender mail = mock(JavaMailSender.class);
    dispatcher(mail, now.plusSeconds(2)).dispatch();
    dispatcher(mail, now.plusSeconds(2)).dispatch();
    verify(mail, times(1)).send(any(SimpleMailMessage.class));
    dispatcher(mail, now.plusSeconds(86401)).dispatch();
    verify(mail, times(2)).send(any(SimpleMailMessage.class));
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM notification_jobs WHERE status='SENT'", Integer.class))
        .isEqualTo(2);
  }

  @Test
  void smtpFailureRetriesAndEventuallyMarksFailed() {
    Instant now = Instant.now();
    inbox.accept(event(1, now.plusSeconds(3600)));
    JavaMailSender mail = mock(JavaMailSender.class);
    doThrow(new MailSendException("offline")).when(mail).send(any(SimpleMailMessage.class));
    for (int i = 0; i < 5; i++) dispatcher(mail, now.plusSeconds(1 + i * 301)).dispatch();
    assertThat(jdbc.queryForObject("SELECT status FROM notification_jobs", String.class))
        .isEqualTo("FAILED");
    assertThat(jdbc.queryForObject("SELECT attempts FROM notification_jobs", Integer.class))
        .isEqualTo(5);
  }

  @Test
  void pastAppointmentsDoNotSendAndMalformedMessageIsRejected() {
    inbox.accept(event(1, Instant.now().minusSeconds(3600)));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notification_jobs", Integer.class))
        .isZero();
    assertThatThrownBy(
            () -> listener.receive(new Message("{}".getBytes(), new MessageProperties())))
        .isInstanceOf(AmqpRejectAndDontRequeueException.class);
  }

  @Test
  void validRawJsonMessageReachesInbox() {
    var event = event(1, Instant.now().plusSeconds(172800));
    listener.receive(
        new Message(
            EventCodec.encode(event).getBytes(java.nio.charset.StandardCharsets.UTF_8),
            new MessageProperties()));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM inbox_events", Integer.class))
        .isEqualTo(1);
  }
}

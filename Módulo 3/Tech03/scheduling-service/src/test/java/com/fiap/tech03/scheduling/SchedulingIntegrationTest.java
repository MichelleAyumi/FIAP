package com.fiap.tech03.scheduling;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:schedulingtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "app.jobs.enabled=false",
      "spring.rabbitmq.dynamic=false",
      "spring.rabbitmq.listener.simple.auto-startup=false"
    })
@ActiveProfiles("demo")
class SchedulingIntegrationTest {
  @Autowired SchedulingService service;
  @Autowired JdbcTemplate jdbc;
  @Autowired WebApplicationContext context;
  @Autowired PlatformTransactionManager tx;
  private MockMvc mvc;
  private final JsonMapper json = JsonMapper.builder().build();

  @BeforeEach
  void setup() {
    jdbc.update("DELETE FROM outbox_events");
    jdbc.update("DELETE FROM appointments");
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  private AppointmentInput input(String patient, Instant time) {
    return new AppointmentInput(patient, "doc-001", time.toString(), 30, "Consulta de rotina");
  }

  private Instant future() {
    return Instant.now().plusSeconds(172800).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
  }

  private String createFixture(String patient, Instant time) {
    String id = UUID.randomUUID().toString();
    jdbc.update(
        "INSERT INTO"
            + " appointments(id,patient_id,doctor_id,starts_at,duration_minutes,reason,clinical_notes,version,created_at,updated_at)"
            + " VALUES (?,?,?,?,30,'Rotina','Historico de exemplo',1,?,?)",
        id,
        patient,
        "doc-001",
        SchedulingService.ts(time),
        SchedulingService.ts(Instant.now()),
        SchedulingService.ts(Instant.now()));
    return id;
  }

  @Test
  void rejectsAnonymousAndWrongPassword() throws Exception {
    mvc.perform(post("/graphql").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/me").with(httpBasic("medico", "wrong")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatesRealBcryptAccount() throws Exception {
    mvc.perform(get("/api/me").with(httpBasic("medico", "medico1234")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles[0]").value("ROLE_DOCTOR"));
    assertThat(
            jdbc.queryForObject(
                "SELECT password_hash FROM app_users WHERE username='medico'", String.class))
        .startsWith("$2");
  }

  @Test
  void graphqlEnforcesOwnershipAndRole() throws Exception {
    String id = createFixture("pac-002", future());
    String query =
        json.writeValueAsString(
            Map.of(
                "query",
                "query($id: ID!){appointment(id:$id){id clinicalNotes}}",
                "variables",
                Map.of("id", id)));
    mvc.perform(
            post("/graphql")
                .with(httpBasic("paciente", "paciente1234"))
                .contentType("application/json")
                .content(query))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"));
    mvc.perform(
            post("/graphql")
                .with(httpBasic("paciente2", "Paciente456"))
                .contentType("application/json")
                .content(query))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.appointment.id").value(id));
    String notes =
        json.writeValueAsString(
            Map.of(
                "query",
                "mutation($id:"
                    + " ID!){updateClinicalNotes(id:$id,expectedVersion:1,notes:\"teste\"){id}}",
                "variables",
                Map.of("id", id)));
    mvc.perform(
            post("/graphql")
                .with(httpBasic("enfermeiro", "enfermeiro1234"))
                .contentType("application/json")
                .content(notes))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"));
  }

  @Test
  void graphqlCreatesUpdatesAndFilters() throws Exception {
    var variables = Map.of("input", input("pac-001", future()));
    String body =
        json.writeValueAsString(
            Map.of(
                "query",
                "mutation($input:AppointmentInput!){createAppointment(input:$input){id version"
                    + " startsAt}}",
                "variables",
                variables));
    String response =
        mvc.perform(
                post("/graphql")
                    .with(httpBasic("enfermeiro", "enfermeiro1234"))
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(jsonPath("$.data.createAppointment.version").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = json.readTree(response).at("/data/createAppointment/id").asText();
    String edit =
        json.writeValueAsString(
            Map.of(
                "query",
                "mutation($id:ID!,$input:AppointmentInput!){updateAppointment(id:$id,expectedVersion:1,input:$input){version}}",
                "variables",
                Map.of("id", id, "input", input("pac-001", future().plusSeconds(3600)))));
    mvc.perform(
            post("/graphql")
                .with(httpBasic("medico", "medico1234"))
                .contentType("application/json")
                .content(edit))
        .andExpect(jsonPath("$.data.updateAppointment.version").value(2));
    createFixture("pac-001", Instant.now().minusSeconds(3600));
    String history =
        json.writeValueAsString(
            Map.of("query", "{history(patientId:\"pac-001\",futureOnly:true){id}}"));
    mvc.perform(
            post("/graphql")
                .with(httpBasic("paciente", "paciente1234"))
                .contentType("application/json")
                .content(history))
        .andExpect(jsonPath("$.data.history.length()").value(1));
  }

  @Test
  @WithMockUser(username = "paciente", roles = "PATIENT")
  void patientCannotMutateOrReadOthers() {
    assertThatThrownBy(() -> service.create(input("pac-001", future())))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> service.history("pac-002", false, 10, 0))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @WithMockUser(username = "medico", roles = "DOCTOR")
  void atomicOutboxAndOptimisticVersion() {
    Appointment created = service.create(input("pac-001", future()));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class))
        .isEqualTo(1);
    Appointment changed = service.updateNotes(created.id(), 1, "Evolucao clinica");
    assertThat(changed.version()).isEqualTo(2);
    assertThatThrownBy(() -> service.updateNotes(created.id(), 1, "stale"))
        .isInstanceOf(ConflictException.class);
    assertThat(service.appointment(created.id()).clinicalNotes()).isEqualTo("Evolucao clinica");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class))
        .isEqualTo(2);
    assertThat(jdbc.queryForList("SELECT payload FROM outbox_events", String.class))
        .allSatisfy(payload -> assertThat(payload).doesNotContain("Evolucao clinica"));
  }

  @Test
  @WithMockUser(roles = "NURSE")
  void validatesDatesAndConflictingIntervals() {
    Instant time = future();
    service.create(input("pac-001", time));
    assertThatThrownBy(() -> service.create(input("pac-002", time.plusSeconds(600))))
        .isInstanceOf(ConflictException.class);
    assertThatThrownBy(() -> service.create(input("pac-001", Instant.now().minusSeconds(1))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(service.create(input("pac-002", time.plusSeconds(1800))).id()).isNotBlank();
  }

  @Test
  void simultaneousBookingsHaveOneWinner() throws Exception {
    Instant time = future();
    var gate = new CountDownLatch(1);
    var won = new AtomicInteger();
    var conflicts = new AtomicInteger();
    try (var pool = Executors.newFixedThreadPool(2)) {
      List<Future<?>> futures = new ArrayList<>();
      for (String patient : List.of("pac-001", "pac-002"))
        futures.add(
            pool.submit(
                () -> {
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(
                              "enfermeiro", "", List.of(new SimpleGrantedAuthority("ROLE_NURSE"))));
                  try {
                    gate.await();
                    service.create(input(patient, time));
                    won.incrementAndGet();
                  } catch (ConflictException e) {
                    conflicts.incrementAndGet();
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      gate.countDown();
      for (Future<?> f : futures) f.get(10, TimeUnit.SECONDS);
    }
    assertThat(won.get()).isEqualTo(1);
    assertThat(conflicts.get()).isEqualTo(1);
  }

  @Test
  @WithMockUser(roles = "DOCTOR")
  void brokerFailureKeepsOutboxForRetry() {
    service.create(input("pac-001", future()));
    EventSender failing =
        (id, payload) -> {
          throw new IllegalStateException("offline");
        };
    var metrics = new SimpleMeterRegistry();
    new OutboxPublisher(jdbc, failing, Clock.systemUTC(), tx, metrics).publish();
    assertThat(jdbc.queryForObject("SELECT attempts FROM outbox_events", Integer.class))
        .isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE sent_at IS NULL", Integer.class))
        .isEqualTo(1);
    new OutboxPublisher(
            jdbc,
            (id, payload) -> {},
            Clock.offset(Clock.systemUTC(), Duration.ofHours(1)),
            tx,
            new SimpleMeterRegistry())
        .publish();
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE sent_at IS NOT NULL", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void metricsAreNotExposedToPatients() throws Exception {
    mvc.perform(get("/actuator/prometheus").with(httpBasic("paciente", "paciente1234")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/actuator/prometheus").with(httpBasic("monitor", "monitor1234")))
        .andExpect(status().isOk());
  }
}

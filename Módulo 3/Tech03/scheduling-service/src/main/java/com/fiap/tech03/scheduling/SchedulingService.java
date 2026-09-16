package com.fiap.tech03.scheduling;

import com.fiap.tech03.contracts.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulingService {
  private final JdbcTemplate jdbc;
  private final Clock clock;

  public SchedulingService(JdbcTemplate jdbc, Clock clock) {
    this.jdbc = jdbc;
    this.clock = clock;
  }

  private static final RowMapper<Appointment> MAPPER =
      (rs, n) ->
          new Appointment(
              rs.getString("id"),
              rs.getString("patient_id"),
              rs.getString("doctor_id"),
              rs.getTimestamp("starts_at").toInstant().toString(),
              rs.getInt("duration_minutes"),
              rs.getString("reason"),
              rs.getString("clinical_notes"),
              rs.getLong("version"),
              rs.getTimestamp("created_at").toInstant().toString(),
              rs.getTimestamp("updated_at").toInstant().toString());

  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','PATIENT')")
  public List<Appointment> history(
      String patientId, Boolean futureOnly, Integer limit, Integer offset) {
    authorizePatient(patientId);
    int size = limit == null ? 50 : limit;
    int start = offset == null ? 0 : offset;
    if (size < 1 || size > 100 || start < 0 || start > 10000)
      throw new IllegalArgumentException("Use limit entre 1 e 100 e offset entre 0 e 10000");
    if (Boolean.TRUE.equals(futureOnly))
      return jdbc.query(
          "SELECT * FROM appointments WHERE patient_id=? AND starts_at>? ORDER BY starts_at,id"
              + " LIMIT ? OFFSET ?",
          MAPPER,
          patientId,
          ts(clock.instant()),
          size,
          start);
    return jdbc.query(
        "SELECT * FROM appointments WHERE patient_id=? ORDER BY starts_at,id LIMIT ? OFFSET ?",
        MAPPER,
        patientId,
        size,
        start);
  }

  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','PATIENT')")
  public Appointment appointment(String id) {
    Appointment a = load(id);
    authorizePatient(a.patientId());
    return a;
  }

  @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
  @Transactional
  public Appointment create(AppointmentInput input) {
    Instant start = validate(input);
    lockAndCheckSchedule(input, start, null);
    String id = UUID.randomUUID().toString();
    Instant now = clock.instant();
    jdbc.update(
        "INSERT INTO"
            + " appointments(id,patient_id,doctor_id,starts_at,duration_minutes,reason,clinical_notes,version,created_at,updated_at)"
            + " VALUES (?,?,?,?,?,?,?,1,?,?)",
        id,
        input.patientId(),
        input.doctorId(),
        ts(start),
        input.durationMinutes(),
        input.reason(),
        "",
        ts(now),
        ts(now));
    Appointment a = load(id);
    enqueue(a, "APPOINTMENT_CREATED");
    return a;
  }

  @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
  @Transactional
  public Appointment update(String id, long expectedVersion, AppointmentInput input) {
    Instant start = validate(input);
    Appointment old = load(id);
    if (!old.patientId().equals(input.patientId()) || !old.doctorId().equals(input.doctorId()))
      throw new IllegalArgumentException(
          "Paciente/medico nao podem ser substituidos; registre uma nova consulta");
    if (!Instant.parse(old.startsAt()).isAfter(clock.instant()))
      throw new IllegalArgumentException("Uma consulta passada nao pode ser reagendada");
    lockAndCheckSchedule(input, start, id);
    int changed =
        jdbc.update(
            "UPDATE appointments SET"
                + " starts_at=?,duration_minutes=?,reason=?,version=version+1,updated_at=? WHERE"
                + " id=? AND version=?",
            ts(start),
            input.durationMinutes(),
            input.reason(),
            ts(clock.instant()),
            id,
            expectedVersion);
    if (changed != 1)
      throw new ConflictException("Consulta alterada por outro usuario; consulte a versao atual");
    Appointment a = load(id);
    enqueue(a, "APPOINTMENT_UPDATED");
    return a;
  }

  @PreAuthorize("hasRole('DOCTOR')")
  @Transactional
  public Appointment updateNotes(String id, long expectedVersion, String notes) {
    if (notes == null || notes.length() > 5000)
      throw new IllegalArgumentException("Historico deve ter ate 5000 caracteres");
    load(id);
    int changed =
        jdbc.update(
            "UPDATE appointments SET clinical_notes=?,version=version+1,updated_at=? WHERE id=? AND"
                + " version=?",
            notes,
            ts(clock.instant()),
            id,
            expectedVersion);
    if (changed != 1)
      throw new ConflictException("Consulta alterada por outro usuario; consulte a versao atual");
    Appointment a = load(id);
    enqueue(a, "APPOINTMENT_UPDATED");
    return a;
  }

  private Instant validate(AppointmentInput i) {
    if (i == null
        || i.patientId() == null
        || i.doctorId() == null
        || i.reason() == null
        || i.reason().isBlank()
        || i.reason().length() > 500
        || i.durationMinutes() < 10
        || i.durationMinutes() > 240)
      throw new IllegalArgumentException(
          "Dados invalidos: duracao entre 10 e 240 min; motivo ate 500 caracteres");
    Instant start;
    try {
      start = Instant.parse(i.startsAt());
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Data deve ser com fuso, por exemplo 2030-01-15T14:00:00Z");
    }
    if (!start.isAfter(clock.instant()))
      throw new IllegalArgumentException("A consulta deve estar no futuro");
    if (jdbc.queryForObject(
            "SELECT COUNT(*) FROM patients WHERE id=?", Integer.class, i.patientId())
        == 0) throw new IllegalArgumentException("Paciente inexistente");
    return start;
  }

  private void lockAndCheckSchedule(AppointmentInput i, Instant start, String excluded) {
    // The doctor row serializes competing bookings across application instances.
    if (jdbc.queryForList(
            "SELECT id FROM doctors WHERE id=? FOR UPDATE", String.class, i.doctorId())
        .isEmpty()) throw new IllegalArgumentException("Medico inexistente");
    // Lock patient too: different doctors cannot book this patient at overlapping times.
    jdbc.queryForList("SELECT id FROM patients WHERE id=? FOR UPDATE", String.class, i.patientId());
    List<Appointment> candidates =
        jdbc.query(
            "SELECT * FROM appointments WHERE (doctor_id=? OR patient_id=?) AND starts_at<?",
            MAPPER,
            i.doctorId(),
            i.patientId(),
            ts(start.plusSeconds(i.durationMinutes() * 60L)));
    if (candidates.stream()
        .anyMatch(
            a ->
                !a.id().equals(excluded)
                    && Instant.parse(a.startsAt())
                        .plusSeconds(a.durationMinutes() * 60L)
                        .isAfter(start)))
      throw new ConflictException("Conflito de horario do medico/paciente");
  }

  private Appointment load(String id) {
    return jdbc.query("SELECT * FROM appointments WHERE id=?", MAPPER, id).stream()
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Consulta nao encontrada"));
  }

  private void authorizePatient(String patientId) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
      String own =
          jdbc.queryForObject(
              "SELECT subject_id FROM app_users WHERE username=?", String.class, auth.getName());
      if (!Objects.equals(own, patientId))
        throw new AccessDeniedException("Acesso permitido apenas as proprias consultas");
    }
  }

  private void enqueue(Appointment a, String type) {
    String email =
        jdbc.queryForObject("SELECT email FROM patients WHERE id=?", String.class, a.patientId());
    AppointmentEvent event =
        new AppointmentEvent(
            UUID.randomUUID().toString(),
            type,
            1,
            a.id(),
            a.version(),
            a.patientId(),
            email,
            a.doctorId(),
            a.startsAt(),
            clock.instant().toString());
    jdbc.update(
        "INSERT INTO outbox_events(id,payload,created_at,next_attempt_at,attempts) VALUES"
            + " (?,?,?,?,0)",
        event.eventId(),
        EventCodec.encode(event),
        ts(clock.instant()),
        ts(clock.instant()));
  }

  static java.sql.Timestamp ts(Instant instant) {

    return java.sql.Timestamp.from(instant);
  }
}

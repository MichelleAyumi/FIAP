package com.fiap.tech03.scheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Synthetic accounts, enabled only with the demo profile. */
@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;
  private final Environment env;

  public DemoData(JdbcTemplate jdbc, PasswordEncoder encoder, Environment env) {
    this.jdbc = jdbc;
    this.encoder = encoder;
    this.env = env;
  }

  @Override
  @Transactional
  public void run(String... args) {
    user("medico", "DOCTOR", "doc-001", "DEMO_DOCTOR_PASSWORD", "medico1234");
    user("enfermeiro", "NURSE", null, "DEMO_NURSE_PASSWORD", "enfermeiro1234");
    user("paciente", "PATIENT", "pac-001", "DEMO_PATIENT_PASSWORD", "paciente1234");
    user("paciente2", "PATIENT", "pac-002", "DEMO_PATIENT2_PASSWORD", "Paciente456");
    user("monitor", "MONITOR", null, "MONITOR_PASSWORD", "monitor1234");
    if (jdbc.queryForObject("SELECT COUNT(*) FROM patients", Integer.class) == 0) {
      jdbc.update(
          "INSERT INTO patients(id,name,email) VALUES (?,?,?)",
          "pac-001",
          "Paciente A",
          "paciente.a@example.test");
      jdbc.update(
          "INSERT INTO patients(id,name,email) VALUES (?,?,?)",
          "pac-002",
          "Paciente  B",
          "paciente.b@example.test");
    }
    if (jdbc.queryForObject("SELECT COUNT(*) FROM doctors", Integer.class) == 0)
      jdbc.update("INSERT INTO doctors(id,name) VALUES (?,?)", "doc-001", "Medico Exemplo");
  }

  private void user(String name, String role, String subject, String key, String fallback) {
    if (jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE username=?", Integer.class, name)
        == 0)
      jdbc.update(
          "INSERT INTO app_users(username,password_hash,role,subject_id) VALUES (?,?,?,?)",
          name,
          encoder.encode(env.getProperty(key, fallback)),
          role,
          subject);
  }
}

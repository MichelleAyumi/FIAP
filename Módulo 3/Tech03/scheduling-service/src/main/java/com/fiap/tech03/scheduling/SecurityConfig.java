package com.fiap.tech03.scheduling;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(JdbcTemplate jdbc) {
    return username ->
        jdbc
            .query(
                "SELECT username, password_hash, role FROM app_users WHERE username = ?",
                (rs, n) ->
                    User.withUsername(rs.getString(1))
                        .password(rs.getString(2))
                        .roles(rs.getString(3))
                        .build(),
                username)
            .stream()
            .findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .hasRole("MONITOR")
                    .requestMatchers("/graphql", "/api/me")
                    .hasAnyRole("DOCTOR", "NURSE", "PATIENT")
                    .anyRequest()
                    .denyAll())
        .httpBasic(withDefaults())
        .build();
  }
}

package com.fiap.tech03.scheduling;

import com.fiap.tech03.contracts.RabbitTopology;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(RabbitTopology.class)
public class SchedulingApplication {
  public static void main(String[] args) {

    SpringApplication.run(SchedulingApplication.class, args);
  }

  @Bean
  Clock clock() {

    return Clock.systemUTC();
  }
}

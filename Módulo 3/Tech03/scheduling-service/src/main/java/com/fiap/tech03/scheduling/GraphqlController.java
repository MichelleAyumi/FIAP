package com.fiap.tech03.scheduling;

import java.util.List;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class GraphqlController {
  private final SchedulingService service;

  public GraphqlController(SchedulingService service) {
    this.service = service;
  }

  @QueryMapping
  public List<Appointment> history(
      @Argument String patientId,
      @Argument Boolean futureOnly,
      @Argument Integer limit,
      @Argument Integer offset) {
    return service.history(patientId, futureOnly, limit, offset);
  }

  @QueryMapping
  public Appointment appointment(@Argument String id) {

    return service.appointment(id);
  }

  @MutationMapping
  public Appointment createAppointment(@Argument AppointmentInput input) {

    return service.create(input);
  }

  @MutationMapping
  public Appointment updateAppointment(
      @Argument String id, @Argument int expectedVersion,  @Argument AppointmentInput input) {
    return service.update(id, expectedVersion, input);
  }

  @MutationMapping
  public Appointment updateClinicalNotes(
      @Argument String id, @Argument int expectedVersion, @Argument String notes) {
    return service.updateNotes(id, expectedVersion, notes);
  }
}

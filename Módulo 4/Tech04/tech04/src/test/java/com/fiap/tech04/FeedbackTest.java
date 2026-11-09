package com.fiap.tech04;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class FeedbackTest {
 @Test void validatesBoundariesAndUrgency() {
  var now = Instant.parse("2026-11-01T12:00:00Z");
  for (int n = 0; n <= 10; n++) {
   var f = Feedback.create(" Aula ", n, now);
   assertEquals(n <= 3 ? "CRITICA" : n <= 6 ? "ATENCAO" : "NORMAL", f.urgencia());
   assertEquals("Aula", f.descricao());
  }
  for (Integer n : new Integer[]{null, -1, 11}) assertThrows(IllegalArgumentException.class, () -> Feedback.create("Aula", n, now));
  for (String d : new String[]{null, " ", "x".repeat(2001)}) assertThrows(IllegalArgumentException.class, () -> Feedback.create(d, 5, now));
 }
}

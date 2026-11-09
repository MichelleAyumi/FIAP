package com.fiap.tech04;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class WeeklyReportTest {
 @Test void weekUsesSaoPauloAndMondayBoundary() {
  var p = WeeklyReport.previousWeek(Instant.parse("2026-11-02T12:00:00Z"));
  assertEquals(Instant.parse("2026-10-26T03:00:00Z"), p.start());
  assertEquals(Instant.parse("2026-11-02T03:00:00Z"), p.end());
 }
 @Test void reportCountsZeroDaysAndComputesAverage() {
  var p = WeeklyReport.previousWeek(Instant.parse("2026-11-02T12:00:00Z"));
  var r = WeeklyReport.create(p, List.of(Feedback.create("A", 2, p.start()), Feedback.create("B", 8, p.start())));
  assertEquals(5.0, r.get("mediaNotas"));
  assertEquals(2, r.get("quantidade"));
  assertEquals(7, ((Map<?, ?>) r.get("quantidadePorDia")).size());
  assertEquals(1L, ((Map<?, ?>) r.get("quantidadePorUrgencia")).get("CRITICA"));
  assertNull(WeeklyReport.create(p, List.of()).get("mediaNotas"));
 }
}

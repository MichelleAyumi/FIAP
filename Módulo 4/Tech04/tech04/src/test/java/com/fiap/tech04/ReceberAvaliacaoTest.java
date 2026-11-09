package com.fiap.tech04;
import com.microsoft.azure.functions.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceberAvaliacaoTest {
 @Test void rejectsInvalidPayloadBeforeSaving() {
  for (String body : List.of("{}", "null", "{", "{\"descricao\":\"A\",\"nota\":2.5}", "{\"descricao\":\"A\",\"nota\":\"2\"}", "{\"descricao\":\"A\",\"nota\":11}")) {
   var repository = mock(FeedbackRepository.class);
   var request = request(body);
   var response = new ReceberAvaliacao(() -> repository).run(request, mock(OutputBinding.class), context());
   assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
   verifyNoInteractions(repository);
  }
 }
 @Test void criticalFeedbackQueuesAlertAndNormalDoesNot() {
  for (int grade : List.of(2, 9)) {
   var repository = mock(FeedbackRepository.class);
   OutputBinding<String> output = mock(OutputBinding.class);
   var response = new ReceberAvaliacao(() -> repository).run(request("{\"descricao\":\"Aula\",\"nota\":" + grade + "}"), output, context());
   assertEquals(HttpStatus.CREATED, response.getStatus());
   verify(repository).save(any());
   if (grade == 2) verify(output).setValue(contains("CRITICA"));
   else verifyNoInteractions(output);
  }
 }
 @Test void storageFailureReturnsServerErrorWithoutAlert() {
  var repository = mock(FeedbackRepository.class);
  doThrow(new IllegalStateException()).when(repository).save(any());
  OutputBinding<String> output = mock(OutputBinding.class);
  var response = new ReceberAvaliacao(() -> repository).run(request("{\"descricao\":\"A\",\"nota\":1}"), output, context());
  assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
  verifyNoInteractions(output);
 }
 private ExecutionContext context() {
  var context = mock(ExecutionContext.class);
  when(context.getLogger()).thenReturn(Logger.getAnonymousLogger());
  return context;
 }
 private HttpRequestMessage<Optional<String>> request(String body) {
  HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
  when(request.getBody()).thenReturn(Optional.of(body));
  when(request.createResponseBuilder(any(HttpStatus.class))).thenAnswer(invocation -> {
   var status = invocation.getArgument(0, HttpStatus.class);
   var builder = mock(HttpResponseMessage.Builder.class, RETURNS_SELF);
   var response = mock(HttpResponseMessage.class);
   when(response.getStatus()).thenReturn(status);
   when(builder.build()).thenReturn(response);
   return builder;
  });
  return request;
 }
}

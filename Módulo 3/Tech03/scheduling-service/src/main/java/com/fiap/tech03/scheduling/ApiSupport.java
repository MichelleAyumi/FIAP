package com.fiap.tech03.scheduling;

import graphql.GraphQLError;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.DataFetchingEnvironment;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.graphql.execution.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Configuration
class GraphqlLimits {
  @Bean
  MaxQueryDepthInstrumentation maxQueryDepth() {

    return new MaxQueryDepthInstrumentation(8);
  }
}

@Component
class GraphqlErrors extends DataFetcherExceptionResolverAdapter {
  @Override
  protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
    ErrorType type;
    if (ex instanceof AccessDeniedException) type = ErrorType.FORBIDDEN;
    else if (ex instanceof IllegalArgumentException || ex instanceof ConflictException)
      type = ErrorType.BAD_REQUEST;
    else if (ex instanceof NoSuchElementException) type = ErrorType.NOT_FOUND;
    else return null;
    return graphql.GraphqlErrorBuilder.newError(env)
        .errorType(type)
        .message(ex.getMessage())
        .build();
  }
}

@RestController
class IdentityController {
  @GetMapping("/api/me")
  Map<String, Object> me(Authentication auth) {
    return Map.of(
        "username",
        auth.getName(),
        "roles",
        auth.getAuthorities().stream().map(Object::toString).toList());
  }
}

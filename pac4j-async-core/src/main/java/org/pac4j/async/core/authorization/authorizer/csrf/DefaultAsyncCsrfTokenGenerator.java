package org.pac4j.async.core.authorization.authorizer.csrf;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Pac4jConstants;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.pac4j.async.core.future.FutureUtils.withFallback;

public class DefaultAsyncCsrfTokenGenerator implements AsyncCsrfTokenGenerator {

    @Override
    public CompletableFuture<String> get(AsyncWebContext context) {

        return withFallback(context.getSessionAttribute(Pac4jConstants.CSRF_TOKEN),
                () -> {
                    final String token = UUID.randomUUID().toString();
                    final CompletableFuture<String> tokenFuture = new CompletableFuture<>();
                    context.setSessionAttribute(Pac4jConstants.CSRF_TOKEN, token)
                            .thenAccept(v -> {
                                tokenFuture.complete(token);
                            });
                    return tokenFuture;
                });

    }

}

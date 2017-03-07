package org.pac4j.async.core.authorization.authorizer.csrf;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Pac4jConstants;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DefaultAsyncCsrfTokenGenerator implements AsyncCsrfTokenGenerator {

    @Override
    public CompletableFuture<String> get(AsyncWebContext context) {

        return applyFallback(context.getSessionAttribute(Pac4jConstants.CSRF_TOKEN),
                () -> {
                    final String token = UUID.randomUUID().toString();
                    final CompletableFuture<String> tokenFuture = new CompletableFuture<>();
                    context.setSessionAttribute(Pac4jConstants.CSRF_TOKEN, token)
                            .thenAccept(v -> tokenFuture.complete(token));
                    return tokenFuture;
                });

    }

    /**
     * Add a fallback future to the case where the result of this future will be null
     * @param originalFuture
     * @return
     */
    private static <T> CompletableFuture<T> applyFallback(final CompletableFuture<T> originalFuture,
                                                        final Supplier<CompletableFuture<T>> fallbackComputation) {
        return originalFuture.thenApply(Optional::ofNullable)
                .thenCompose(o -> o.map(t -> CompletableFuture.completedFuture(t))
                        .orElseGet(fallbackComputation));
    }
}

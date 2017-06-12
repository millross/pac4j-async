package org.pac4j.async.core.future;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Utility functions for managing CompletableFutures
 */
public class FutureUtils {

    /**
     * Add a fallback future to the case where the result of this future will be null
     * @param originalFuture - the future which could result in a null value
     * @param fallbackFutureSupplier - supplier which will supply another completable future to be applied if the result
     *                              of originalFuture is null
     * @return - CompletableFuture which will complete with the result of originalFuture if not null, otherwise the
     * result of the future provided by fallbackFutureSupplier
     */
    public static <T> CompletableFuture<T> withFallback(final CompletableFuture<T> originalFuture,
                                                        final Supplier<CompletableFuture<T>> fallbackFutureSupplier) {
        return originalFuture.thenApply(Optional::ofNullable)
                .thenCompose(o -> o.map(t -> CompletableFuture.completedFuture(t))
                        .orElseGet(fallbackFutureSupplier));
    }

    public static CompletableFuture<Boolean> shortCircuitedFuture(final Stream<Supplier<CompletableFuture<Boolean>>> futureSuppliers,
                                                                  final Boolean fallbackOn) {
        return futureSuppliers.reduce(completedFuture(!fallbackOn),
                (f, a) -> f.thenCompose(b -> (b != fallbackOn) ? a.get() : completedFuture(fallbackOn)),
                (bf1, bf2) -> bf1.thenCompose(b  -> (b != fallbackOn) ? bf2 : CompletableFuture.completedFuture(fallbackOn)));
    }

}

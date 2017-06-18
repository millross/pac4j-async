package org.pac4j.async.core.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

/**
 * Utility functions for managing CompletableFutures
 */
public class FutureUtils {

    protected static final Logger logger = LoggerFactory.getLogger(FutureUtils.class);

    /**
     * Simple wrapper for CompletableFuture.allOf to act on a list of futures
     * @param futures
     * @param <T>
     * @return CompletableFuture
     * @see CompletableFuture#allOf
     */
    public static <T> CompletableFuture<Void> allOf(Stream<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Given a list of completable futures, convert them to a completable future of a list of all futures.
     * If any should fail, then the whole future should fail.
     * @param futureList - list of futures to combine into single list future
     * @param <T>
     * @return
     */
    public static <T> CompletableFuture<List<T>> combineFuturesToList(List<CompletableFuture<T>> futureList) {

        logger.info("combineFuturesToList() called");
        return allOf(futureList.stream())
                // Convert to a list of futures
                .thenApply(v -> {
                    logger.info("All futures now completed");
                    return v;
                })
                .thenApply(v -> futureList
                        .stream()
                        .map(f -> f.join())
                        .collect(toList()));
    }

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

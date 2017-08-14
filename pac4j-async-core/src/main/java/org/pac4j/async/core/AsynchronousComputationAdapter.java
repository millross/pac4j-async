package org.pac4j.async.core;

import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Interface defining how to take a computation which is synchronous (whether thread-blocking or non-blocking) and make
 * it asynchronous. Note that a static method is to be offered for wrapping a non-blocking conputation, for the very
 * simple reason that this is not dependent on the pac4j implementation. Realistically this will have to be wrapped
 * on a per-implementation basis where context is important.
 *
 * However, the blocking wrapper will vary between asynchronous frameworks and it's important that the correct one be
 * used for a given framework. Therefore this is expressed as a method definition only, as it will need to be
 * implemented on a per-network basis. It _may_ be that we ofer some standard ones which can be reused across
 * frameworks.
 */
public interface AsynchronousComputationAdapter {

    static <T> CompletableFuture<T> fromNonBlocking(final Supplier<T> syncComputation) {
        return CompletableFuture.completedFuture(syncComputation.get());
    }

    static CompletableFuture<Void> fromNonBlocking(final Runnable syncComputation) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        syncComputation.run();
        future.complete(null);
        return future;
    }

    /**
     * Return a completableFuture obtained by running the specified non-blocking computation on the context associated
     * with this AsynchronousComputationAdapter. This is important where a computation must occur on a specific context
     * (usually used to ensure it will occur on the correct thread in asynchronous frameworks such as vert.x).
     *
     * Note that in general a context will be associated with only one thread, therefore it is sensible to offer an
     * AsynchronousComputationAdapter per context-related thread. The "OnContext" converters are to be used when a computation
     * will mutate state on that context, the aim being to preserve thread-safety.
     *
     * @param syncComputation computation which will return the value with which to complete the future
     * @param <T> The type with which the CompletableFuture will complete
     * @return CompletableFuture which will complete with the return value of the computation.
     */
    default <T> CompletableFuture<T> fromNonBlockingOnContext(final Supplier<T> syncComputation) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        getExecutionContext().runOnContext(() -> future.complete(syncComputation.get()));
        return future;
    }

    /**
     * Return a completableFuture obtained by running the specified void-returning non-blocking computation on the
     * context associated with this AsynchronousComputationAdapter. This is important where a computation must occur on
     * a specific context (usually used to ensure it will occur on the correct thread in asynchronous frameworks such
     * as vert.x).
     *
     * Note that in general a context will be associated with only one thread, therefore it is sensible to offer an
     * AsynchronousComputationAdapter per context-related thread. The "OnContext" converters are to be used when a computation
     * will mutate state on that context, the aim being to preserve thread-safety.
     *
     * param syncComputation computation which will return the value with which to complete the future
     * @return CompletableFuture which will complete with the return value of the computation.
     */
    default CompletableFuture<Void> fromNonBlockingOnContext(final Runnable syncComputation) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        getExecutionContext().runOnContext(() -> {
            syncComputation.run();
            future.complete(null);
        });
        return future;
    }

    <T> CompletableFuture<T> fromBlocking(final Supplier<T> syncComputation);
    default CompletableFuture<Void> fromBlocking(final Runnable syncComputation) {
        return fromBlocking(() -> {
            syncComputation.run();
            return null;
        });
    }

    AsyncPac4jExecutionContext getExecutionContext();
}

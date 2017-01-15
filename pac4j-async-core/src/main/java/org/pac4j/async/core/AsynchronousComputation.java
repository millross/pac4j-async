package org.pac4j.async.core;

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
public interface AsynchronousComputation {

    static <T> CompletableFuture<T> fromNonBlocking(final Supplier<T> syncComputation) {
        return CompletableFuture.completedFuture(syncComputation.get());
    }

    <T> CompletableFuture<T> fromBlocking(final Supplier<T> syncComputation);
}

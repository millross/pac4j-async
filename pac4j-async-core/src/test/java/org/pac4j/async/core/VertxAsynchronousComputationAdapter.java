package org.pac4j.async.core;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 *
 */
public class VertxAsynchronousComputationAdapter implements AsynchronousComputation {

    private final Vertx vertx;
    private final VertxContextRunner vertxContextRunner;

    public VertxAsynchronousComputationAdapter(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.vertxContextRunner = new VertxContextRunner(context);
    }

    @Override
    public <T> CompletableFuture<T> fromBlocking(Supplier<T> syncComputation) {

        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        vertx.<T>executeBlocking(future -> future.complete(syncComputation.get()),
                false,
                asyncResult -> {
                    if (asyncResult.succeeded()) {
                        completableFuture.complete(asyncResult.result());
                    } else {
                        completableFuture.completeExceptionally(asyncResult.cause());
                    }
                }
        );

        return completableFuture;

    }

    @Override
    public AsyncPac4jExecutionContext getExecutionContext() {
        return vertxContextRunner;
    }

}

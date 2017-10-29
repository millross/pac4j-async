package org.pac4j.async.vertx;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.async.vertx.execution.context.VertxAsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 *
 */
public class VertxAsynchronousComputationAdapter implements AsynchronousComputationAdapter {
    private final Vertx vertx;
    private final VertxAsyncPac4jExecutionContext executionContext;

    public VertxAsynchronousComputationAdapter(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.executionContext = new VertxAsyncPac4jExecutionContext(context);
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
        return executionContext;
    }

}

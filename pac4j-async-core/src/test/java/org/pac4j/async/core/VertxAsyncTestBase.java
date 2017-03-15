package org.pac4j.async.core;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.pac4j.async.core.authorization.generator.VertxContextRunner;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Base class for using vert.x as the asynchronous framework for testing our designs. It sets up the core elements to
 * run a vert.x test, including a blocking wrapper factory for blocking synchronous code. Abstract as it shouldn't be
 * run as a test.
 *
 */
@RunWith(VertxUnitRunner.class)
public abstract class VertxAsyncTestBase {

    protected static final long DEFAULT_DELAY = 250;
    protected AsyncPac4jExecutionContext executionContext = null;

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public final void applyExceptionHandling(final TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
        executionContext = new VertxContextRunner(rule.vertx().getOrCreateContext());
    }

    protected <T> CompletableFuture<T> delayedResult(final Supplier<T> supplier) {
        return delayedResult(DEFAULT_DELAY, supplier);
    }

    private <T> CompletableFuture<T> delayedResult(final long delay, final Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<T>();
        rule.vertx().runOnContext(l -> {
            future.complete(supplier.get());
        });
        return future;
    }

    protected static class AsynchronousVertxComputation implements AsynchronousComputation {

        final Vertx vertx;
        final VertxContextRunner vertxContextRunner;

        public AsynchronousVertxComputation(Vertx vertx, Context context) {
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


}

package org.pac4j.async.core;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
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
    protected VertxAsynchronousComputationAdapter asynchronousComputationAdapter = null;
    protected AsyncPac4jExecutionContext executionContext = null;

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public final void applyExceptionHandling(final TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
        this.asynchronousComputationAdapter = new VertxAsynchronousComputationAdapter(rule.vertx(), rule.vertx().getOrCreateContext());
        this.executionContext = asynchronousComputationAdapter.getExecutionContext();
    }

    protected  <T> CompletableFuture<T> delayedResult(final Supplier<T> supplier) {
        return delayedResult(DEFAULT_DELAY, supplier);
    }

    protected <T> CompletableFuture<T> delayedResult(final long delay, final Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<T>();
        rule.vertx().setTimer(delay, l -> rule.vertx().runOnContext(v -> future.complete(supplier.get())));
        return future;
    }

    protected <T> CompletableFuture<T> delayedException (final long delay, final Exception e) {
        return delayedException(delay, () -> e);
    }

    protected <T> CompletableFuture<T> delayedException (final long delay, final Supplier<Exception> exceptionSupplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        rule.vertx().setTimer(delay, l -> rule.vertx().runOnContext(v -> {
            future.completeExceptionally(exceptionSupplier.get());
        }));
        return future;
    }

    protected <T> CompletableFuture<Void> assertSuccessfulEvaluation (final CompletableFuture<T> future,
                                                                      final Consumer<T> assertions,
                                                                      final Async async){
        return future.handle((v, t) -> {
            if (t != null) {
                executionContext.runOnContext(ExceptionSoftener.softenRunnable(() -> {
                    if (t instanceof CompletionException) {throw t.getCause();
                }else {
                                throw t;
                            }
                        }));
            } else {
                executionContext.runOnContext(() -> {
                    assertions.accept(v);
                    async.complete();
                });
            }
            return null;
        });
    }

    protected static class AsynchronousVertxComputation implements AsynchronousComputationAdapter {

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

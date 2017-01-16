package org.pac4j.async.core;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Simple tests around converting synchronous computations into asynchronous computations. We use vertx-unit test
 * classes to create our asynchronous environment
 */
@RunWith(VertxUnitRunner.class)
public class AsynchronousComputationTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public final void applyExceptionHandling(final TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
    }

    /*
    Test that a completable future around a non-blocking piece of code will complete immediately on-thread as if it
    had been called directly. In a vertx-like cont
     */
    @Test(timeout = 1000)
    public void testConvertFromNonBlockingSynchronous(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();
        final int input = 1;

        AsynchronousComputation.fromNonBlocking(() -> incrementNow(input))
            .thenAccept(i -> {
                context.runOnContext(x -> {
                    assertThat(i, is(input + 1));
                    async.complete();
                });
            });

    }

    /*
    Test that a completable future around a nonpiece of code will complete immediately on-thread as if it
    had been called directly. In a vertx-like cont
     */
    @Test(timeout = 1000)
    public void testConvertFromBlockingSynchronous(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();
        final int input = 1;

        new AsynchronousVertxComputation(rule.vertx())
                .fromBlocking(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return incrementNow(input);
                })
                .thenAccept(i -> context.runOnContext(x -> {
                    assertThat(i, is(input + 1));
                    async.complete();
                }));

    }

    public Integer incrementNow(final Integer i) {
        return i + 1;
    }

    private static class AsynchronousVertxComputation implements AsynchronousComputation {

        final Vertx vertx;

        private AsynchronousVertxComputation(Vertx vertx) {
            this.vertx = vertx;
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
    }

}
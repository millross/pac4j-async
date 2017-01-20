package org.pac4j.async.core;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.pac4j.async.core.authorization.generator.VertxContextRunner;
import org.pac4j.async.core.context.ContextRunner;

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

    protected ContextRunner contextRunner = null;

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public final void applyExceptionHandling(final TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
        contextRunner = new VertxContextRunner(rule.vertx().getOrCreateContext());
    }

    protected static class AsynchronousVertxComputation implements AsynchronousComputation {

        final Vertx vertx;

        public AsynchronousVertxComputation(Vertx vertx) {
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

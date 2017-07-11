package org.pac4j.async.core.exception.handler;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class DefaultAsyncExceptionHandlerTest extends VertxAsyncTestBase {

    final AtomicReference<Throwable> thrown = new AtomicReference<>(null);
    final AsyncExceptionHandler<Integer> exceptionHandler = new DefaultAsyncExceptionHandler<>();
    final AsyncWebContext webContext = MockAsyncWebContextBuilder.from(rule.vertx(), pac4jExecutionContext()).build();

    @Test
    public void successfulResult(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<Integer> future = exceptionHandler.applyExceptionHandling(delayedResult(() -> 2),
                webContext);
        future.thenAccept(i -> {
            assertThat(i, is(2));
            assertThat(thrown.get(), is(nullValue()));
            async.complete();
        });
    }

    @Test
    public void exceptionalResult(final TestContext testContext) {
        final IntentionalException e = new IntentionalException();
        final Async async = testContext.async();
        final CompletableFuture<Integer> future = exceptionHandler.applyExceptionHandling(delayedException(125, e),
                webContext);
        future.thenAccept(i -> {
            assertThat(i, is(nullValue()));
            assertThat(thrown.get(), is(e));
            async.complete();
        });
    }

    final AsyncPac4jExecutionContext pac4jExecutionContext() {
        return operation -> {
            try {
                operation.run();
            } catch (Throwable t) {
                thrown.set(t);
            }
        };
    }

}
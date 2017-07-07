package org.pac4j.async.core.exception.handler;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.exception.HttpAction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.aol.cyclops.invokedynamic.ExceptionSoftener.softenBiFunction;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Test for the Async exception handler mechanism in vert.x
 */
public class AsyncExceptionHandlerTest extends VertxAsyncTestBase {

    /**
     * Test which ensures that when the exceptional completion is not going to be wrapped in a CompletionException,
     * the unwrapper handles the situation properly.
     * @param testContext
     * @throws Exception
     */
    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testFailureWithoutSubsequentComputation(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        applyAsyncExceptionHandlingTo(future);

        rule.vertx().setTimer(200, l -> future.completeExceptionally(new IntentionalException()));
    }

    /**
     * Test which ensures that when the exceptional completion is going to be wrapped in a CompletionException,
     * the unwrapper handles the situation properly.
     * @param testContext
     * @throws Exception
     */
    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testFailureWithSubsequentComputation(final TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final CompletableFuture<Integer> initialFuture = new CompletableFuture<>();
        final CompletableFuture<Integer> followingProcessing = initialFuture
                .thenCompose(t -> CompletableFuture.completedFuture(t + 1));

        applyAsyncExceptionHandlingTo(followingProcessing);
        rule.vertx().setTimer(200, l -> initialFuture.completeExceptionally(new IntentionalException()));
    }

    @Test(timeout = 1000)
    public void testExtractionHttpActionOnSuccess(final TestContext testContext) {

        final Async async = testContext.async();
        final AsyncWebContext asyncWebContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
        final HttpAction okAction = HttpAction.ok("No op", asyncWebContext);
        final CompletableFuture<HttpAction> okFuture = delayedResult(() -> okAction)
                .handle(softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction));
        okFuture.thenAccept(a -> {
            assertThat(a.getCode(), is(200));
            async.complete();
        });
    }

    @Test(timeout = 1000)
    public void testExtractHttpActionOnHttpActionExceptionalCompletion(final TestContext testContext) {
        final Async async = testContext.async();
        final AsyncWebContext asyncWebContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
        final HttpAction forbiddenAction = HttpAction.forbidden("Forbidden", asyncWebContext);
        final CompletableFuture<HttpAction> okFuture = this.<HttpAction>delayedException(100, forbiddenAction)
                .handle(softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction));
        okFuture.thenAccept(a -> {
            assertThat(a.getCode(), is(403));
            async.complete();
        });
    }

    @Test(timeout = 1000)
    public void testExtractRuntimeExceptionOnExceptionalCompletion(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<HttpAction> okFuture = this.<HttpAction>delayedException(100, new IntentionalException())
                .handle(softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction));
        okFuture.handle((a, t) -> {
            assertThat(a, is(nullValue()));
            AsyncExceptionHandler.handleException(t, unwrapped -> {
                assertThat(unwrapped instanceof IntentionalException, is(true));
                async.complete();
            });
            return null;
        });
    }

    @Test(timeout = 1000)
    public void testHttpActionThrownDuringResultTransformation(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<HttpAction> future = this.<Integer>delayedResult(100, () -> 2)
                .<HttpAction>thenApply(v -> {
                    throw new IntentionalException();
                })
                .handle(softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction));
        assertExceptionalCompletion(future, async, t -> assertThat(t instanceof IntentionalException, is(true)));

    }

    @Test(timeout = 1000)
    public void testOtherExceptionThrownDuringResultTransformation(final TestContext testContext) {
        final Async async = testContext.async();
        final AsyncWebContext asyncWebContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
        final HttpAction forbiddenAction = HttpAction.forbidden("Forbidden", asyncWebContext);
        final CompletableFuture<HttpAction> okFuture = this.<Integer>delayedResult(100, () -> 2)
                .<HttpAction>thenApply(v -> {
                    throw forbiddenAction;
                })
                .handle(softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction));
        okFuture.thenAccept(a -> {
            assertThat(a.getCode(), is(403));
            async.complete();
        });

    }

    private <T> void assertExceptionalCompletion(final CompletableFuture<T> future, final Async async,
                                             final Consumer<Throwable> exceptionAssertions) {
        future.handle((a, t) -> {
            assertThat(a, is(nullValue()));
            AsyncExceptionHandler.handleException(t, unwrapped -> {
                exceptionAssertions.accept(unwrapped);
                async.complete();
            });
            return null;
        });

    }

    private void applyAsyncExceptionHandlingTo(CompletableFuture<Integer> future) {
        future.whenComplete((i, t) -> {
           if (i != null) {
               throw new RuntimeException("Future was supposed to fail");
           } else {
               AsyncExceptionHandler.handleException(t, e -> executionContext.runOnContext(() -> {
                   if (e instanceof IntentionalException) {
                       throw ((IntentionalException) e);
                   } else {
                       throw new RuntimeException(e);
                   }
               }));
           }
        });
    }

}
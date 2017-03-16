package org.pac4j.async.core.exception.handler;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.VertxAsyncTestBase;

import java.util.concurrent.CompletableFuture;

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
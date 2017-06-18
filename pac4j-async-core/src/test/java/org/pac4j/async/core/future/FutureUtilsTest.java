package org.pac4j.async.core.future;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.VertxAsyncTestBase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.pac4j.async.core.future.FutureUtils.shortCircuitedFuture;

/**
 *
 */
public class FutureUtilsTest extends VertxAsyncTestBase {

    final AtomicInteger failureStep = new AtomicInteger(0);

    @Before
    public void setup() {
        // Reset the failure step to zero
        failureStep.set(0);
    }

    // empty short circuited future
    @Test
    public void testEmptyStream(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(new LinkedList<Supplier<CompletableFuture<Boolean>>>()
                .stream(), false)
            .thenAccept(b -> executionContext.runOnContext(() -> {
                assertThat(b, is(true));
                assertThat(failureStep.get(), is(0));
                async.complete();
            }));
    }

    // short circuited future, both of two will fail, but update for first step will occur
    @Test
    public void testBothOfTwoFailing(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, false),
                indexedFutureSupplier(2, false))
                .stream(), false)
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(false));
                    assertThat(failureStep.get(), is(1));
                    async.complete();
                }));
    }

    // short circuited future: first of two fails, second of two succeeds, will result in false, but value for first
    // step will be set
    @Test
    public void testOnlyFirstOfTwoFailing(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, false),
                indexedFutureSupplier(2, true))
                .stream(), false)
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(false));
                    assertThat(failureStep.get(), is(1));
                    async.complete();
                }));
    }

    // short circuited future: first of two fails, second of two succeeds, will result in false, but value for first
    // step will be set
    @Test
    public void testShortCircuitOnFirstOfTwoWithFallbackOnTrue(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, true),
                indexedFutureSupplier(2, false))
                .stream(), true)
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(true));
                    assertThat(failureStep.get(), is(1));
                    async.complete();
                }));
    }

    // short circuited future: first of two succeeds, second of two fails
    @Test
    public void testShortCircuitOnSecond(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, true),
                indexedFutureSupplier(2, false))
                .stream(), false)
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(false));
                    assertThat(failureStep.get(), is(2));
                    async.complete();
                }));
    }

    // short circuited future: both of two will succeed
    @Test
    public void testBothOfTwoPassing(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, true),
                indexedFutureSupplier(2, true))
                .stream(), false)
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(true));
                    assertThat(failureStep.get(), is(2));
                    async.complete();
                }));
    }

    @Test
    public void combineListSuccess(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        final List<CompletableFuture<Integer>> futureList = Arrays.asList(delayedFuture(2, 100),
                delayedFuture(3, 300),
                delayedFuture(1, 200));
        FutureUtils.combineFuturesToList(futureList)
                .thenAccept(l -> {
                   assertThat(l, is(Arrays.asList(2, 3, 1)));
                   async.complete();
                });
    }

    @Test(timeout = 1000, expected = IntentionalException.class)
    public void combineListWithFailure(final TestContext testContext) throws Throwable {
        final Async async = testContext.async();
        final List<CompletableFuture<Integer>> futureList = Arrays.asList(delayedFuture(2, 100),
                delayedException(300, new IntentionalException()),
                delayedFuture(1, 200));
        FutureUtils.combineFuturesToList(futureList)
                .whenComplete((v, t)-> {
                    if (t != null) {
                        rule.vertx().getOrCreateContext().runOnContext(x -> {
                            throw (RuntimeException) t.getCause();
                        });
                    }
                });
    }

    private CompletableFuture<Integer> delayedFuture(final int value, final int delayMs) {
        return delayedResult(delayMs, () -> value);
    }

    private Supplier<CompletableFuture<Boolean>> indexedFutureSupplier(final int index, final boolean value) {
        return () -> delayedResult(() -> {
            failureStep.set(index);
            return value;
        });

    }
}
package org.pac4j.async.core.future;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.VertxAsyncTestBase;

import java.util.Arrays;
import java.util.LinkedList;
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
                .stream())
            .thenAccept(b -> contextRunner.runOnContext(() -> {
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
                .stream())
                .thenAccept(b -> contextRunner.runOnContext(() -> {
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
                .stream())
                .thenAccept(b -> contextRunner.runOnContext(() -> {
                    assertThat(b, is(false));
                    assertThat(failureStep.get(), is(1));
                    async.complete();
                }));
    }

    // short circuited future: first of two succeeds, second of two fails
    @Test
    public void testOnlySecondOfTwoFailing(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        shortCircuitedFuture(Arrays.asList(
                indexedFutureSupplier(1, true),
                indexedFutureSupplier(2, false))
                .stream())
                .thenAccept(b -> contextRunner.runOnContext(() -> {
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
                .stream())
                .thenAccept(b -> contextRunner.runOnContext(() -> {
                    assertThat(b, is(true));
                    assertThat(failureStep.get(), is(2));
                    async.complete();
                }));
    }

    private Supplier<CompletableFuture<Boolean>> indexedFutureSupplier(final int index, final boolean value) {
        return () -> delayedResult(() -> {
            failureStep.set(index);
            return value;
        });

    }
}
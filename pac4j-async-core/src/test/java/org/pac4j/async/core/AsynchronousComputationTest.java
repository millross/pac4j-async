package org.pac4j.async.core;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import io.vertx.core.Context;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Simple tests around converting synchronous computations into asynchronous computations. We use vertx-unit test
 * classes to create our asynchronous environment
 */
public class AsynchronousComputationTest extends VertxAsyncTestBase {

    /*
    Test that a completable future around a non-blocking piece of code will complete immediately on-thread as if it
    had been called directly. In a vertx-like cont
     */
    @Test(timeout = 1000)
    public void testConvertFromNonBlockingSynchronous(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        final Async async = testContext.async();
        final int input = 1;

        AsynchronousComputation.fromNonBlocking(() -> incrementNow(input))
            .thenAccept(i -> context.runOnContext(x -> {
                assertThat(i, is(input + 1));
                async.complete();
            }));

    }

    @Test(timeout = 1000)
    public void  testConvertFromNonBlockingSynchronousRunnable(final TestContext testContext) {
        final Context context = rule.vertx().getOrCreateContext();
        final Async async = testContext.async();
        final AtomicInteger mutable = new AtomicInteger(1);

        AsynchronousComputation.fromNonBlocking(() -> mutable.set(10))
            .thenRun(() -> context.runOnContext(v -> {
                assertThat(mutable.get(), is(10));
                async.complete();
            }));
    }

    /**
     * Test for failing non-blocking synchronous computation with unchecked exception
     * @param testContext
     */
    @Test(timeout = 1000, expected=IntentionalException.class)
    public void testFromNonBlockingSynchronousFailureUncheckedException(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        final Async async = testContext.async();
        final int input = 1;

        AsynchronousComputation.fromNonBlocking(() -> IntentionalException.throwException(input))
                .thenAccept(i -> {
                    context.runOnContext(x -> {
                        assertThat(i, is(input + 1));
                        async.complete();
                    });
                });

    }

    /**
     * Test for failing non-blocking synchronous computation with checked exception
     * @param testContext
     */
    @Test(timeout = 1000, expected=CheckedIntentionalException.class)
    public void testFromNonBlockingSynchronousFailureCheckedException(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        final Async async = testContext.async();
        final int input = 1;

        AsynchronousComputation.fromNonBlocking(ExceptionSoftener.softenSupplier(() -> CheckedIntentionalException.throwException()))
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

        new AsynchronousVertxComputation(rule.vertx(), context)
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

    /*
    Test that a completable future around a nonpiece of code will complete immediately on-thread as if it
    had been called directly. In a vertx-like cont
     */
    @Test(timeout = 1000)
    public void testConvertFromBlockingSynchronousRunnable(final TestContext testContext) {

        final Context context = rule.vertx().getOrCreateContext();
        final Async async = testContext.async();
        final AtomicInteger mutable = new AtomicInteger(1);

        new AsynchronousVertxComputation(rule.vertx(), context)
                .fromBlocking(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    mutable.set(10);
                })
                .thenRun(() -> context.runOnContext(v -> {
                    assertThat(mutable.get(), is(10));
                    async.complete();
                }));

    }

    /*
    Test that a completable future around a blocking piece of code will fail correctly
     */
    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testFromBlockingSynchronousFailure(final TestContext testContext) throws Throwable{

        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();
        final int input = 1;

        new AsynchronousVertxComputation(rule.vertx(), context)
                .fromBlocking(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return IntentionalException.throwException(input);
                })
                .whenComplete((i, t) -> {
                    if (i != null) {
                        context.runOnContext(x -> {
                            assertThat(i, is(input + 1));
                            async.complete();
                        });
                    } else {
                        // This will need to be delegated to a specific pac4j exception handler at the relevant points,
                        // this exception handler will need to use the type of exception caught to act according to
                        // whether it's an unexpected exception, a technical exception or an http action (or
                        // possibly a credentials exception)
                        context.runOnContext(v -> {
                            if (t instanceof IntentionalException) {
                                // Note that the cast is not actually redundant - this will log as it hits the default
                                // exception handler, but will be expected by the test
                                throw (IntentionalException) t;
                            }
                            throw new RuntimeException(t);
                        });
                    }
                });

    }

    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testAsyncExceptionalCompletion(final TestContext testContext) throws Throwable {

        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();
        final int input = 1;

        final CompletableFuture<Integer> future = new CompletableFuture<>();
        rule.vertx().setTimer(500, i -> {
            future.completeExceptionally(new IntentionalException());
        });
        future.whenComplete((i, t) -> {
            if (i != null) {
                context.runOnContext(v -> {
                    throw new RuntimeException("Future should not have completed successfully");
                });
            } else {
                context.runOnContext(v -> {
                    if (t instanceof IntentionalException) {
                        // Note that the cast is not actually redundant - this will log as it hits the default
                        // exception handler, but will be expected by the test
                        throw (IntentionalException) t;
                    }
                    throw new RuntimeException(t);
                });
            }
        });

    }

    @Test(timeout = 1000)
    public void testFromNonBlockingRunnableOnContext(final TestContext testContext) {
        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();

        final List<Integer> ints = Arrays.asList(1);
        final CompletableFuture<Void> future = new AsynchronousVertxComputation(rule.vertx(), context)
                .fromNonBlockingOnContext(() -> {
                    ints.set(0, 2);
                });
        future.thenAccept(v -> {
            assertThat(ints, is(Arrays.asList(2)));
            async.complete();
        });
    }

    @Test(timeout = 1000)
    public void testFromNonBlockingSupplierOnContext(final TestContext testContext) {
        final Context context = rule.vertx().getOrCreateContext();
        Async async = testContext.async();

        final List<Integer> ints = Arrays.asList(1);
        final CompletableFuture<Integer> future = new AsynchronousVertxComputation(rule.vertx(), context)
                .fromNonBlockingOnContext(() -> {
                    ints.set(0, 2);
                    return 1;
                });
        future.thenAccept(i -> {
            assertThat(i, is(1));
            assertThat(ints, is(Arrays.asList(2)));
            async.complete();
        });
    }

    public Integer incrementNow(final Integer i) {
        return i + 1;
    }


}
package org.pac4j.async.core.authorization.generator;

import io.vertx.core.Context;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Simple test for the AsyncAuthorizationGenerator interface
 */
@RunWith(VertxUnitRunner.class)
public class AsyncAuthorizationGeneratorTest extends VertxAsyncTestBase {

    @Test(timeout = 1000)
    public void testFromNonBlocking(final TestContext context) throws Exception {
        runAsyncAuthGeneratorTest(context, monitor -> AsyncAuthorizationGenerator.fromNonBlockingAuthorizationGenerator(
                nonBlockingAuthGenerator(monitor)
        ));
    }

    @Test(timeout = 1000)
    public void testFromBlocking(final TestContext context) throws Exception {
        runAsyncAuthGeneratorTest(context, monitor -> AsyncAuthorizationGenerator.fromBlockingAuthorizationGenerator(
                blockingAuthGenerator(monitor), new AsynchronousVertxComputation(rule.vertx())
        ));
    }

    @Test(timeout = 1000)
    public void testAsyncNonBlocking(final TestContext context) throws Exception {

        final Function<AtomicInteger, AsyncAuthorizationGenerator<TestProfile>> asyncAuthGenFactory =
                (AtomicInteger monitor) -> (AsyncAuthorizationGenerator<TestProfile>) profile -> {
                    final CompletableFuture<Consumer<TestProfile>> completableFuture = new CompletableFuture<>();
                    rule.vertx().setTimer(250, l -> {
                        monitor.set(1);
                        completableFuture.complete(null);
                    });
                    return completableFuture;
                };

        runAsyncAuthGeneratorTest(context, asyncAuthGenFactory);
    }

    public void runAsyncAuthGeneratorTest(final TestContext context,
                                          final Function<AtomicInteger, AsyncAuthorizationGenerator<TestProfile>> authGenFactory) throws Exception {
        final Context vertxContext = rule.vertx().getOrCreateContext();
        final Async async = context.async();
        final AtomicInteger monitor = new AtomicInteger(0);

        final CompletableFuture<Consumer<TestProfile>> completableFuture = authGenFactory.apply(monitor).generate(new TestProfile());

        completableFuture.thenAccept(i -> contextRunner.runOnContext(() -> {
            assertThat(monitor.get(), is(1));
            async.complete();

        }));
    }

    AuthorizationGenerator<TestProfile> nonBlockingAuthGenerator(final AtomicInteger output) {
        return profile -> output.set(1);
    }

    AuthorizationGenerator<TestProfile> blockingAuthGenerator(final AtomicInteger output) {
        return profile -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            output.set(1);
        };
    }

}
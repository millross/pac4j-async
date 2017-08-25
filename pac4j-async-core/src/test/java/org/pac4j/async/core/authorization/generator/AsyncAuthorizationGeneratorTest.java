package org.pac4j.async.core.authorization.generator;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContextBase;

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
    public void testFromBlocking(final TestContext testContext) throws Exception {
        runAsyncAuthGeneratorTest(testContext, monitor -> AsyncAuthorizationGenerator.fromBlockingAuthorizationGenerator(
                blockingAuthGenerator(monitor)));
    }

    @Test(timeout = 1000)
    public void testAsyncNonBlocking(final TestContext context) throws Exception {

        final Function<AtomicInteger, AsyncAuthorizationGenerator<TestProfile>> asyncAuthGenFactory =
                (AtomicInteger monitor) -> (AsyncAuthorizationGenerator<TestProfile>) (webContext, profile) -> {
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
        final Async async = context.async();
        final AtomicInteger monitor = new AtomicInteger(0);
        // Need to mock the AsyncWebContext for this to work
        final AsyncWebContext webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .build();

        final CompletableFuture<Consumer<TestProfile>> completableFuture = authGenFactory.apply(monitor)
                .generate(webContext, TestProfile.from(new TestCredentials("name", "password")));

        completableFuture.thenAccept(i -> executionContext.runOnContext(() -> {
            assertThat(monitor.get(), is(1));
            async.complete();

        }));
    }

    AuthorizationGenerator<WebContextBase<?>, TestProfile> nonBlockingAuthGenerator(final AtomicInteger output) {
        return (context, profile) -> {
            output.set(1);
            return profile;
        };
    }

    AuthorizationGenerator<WebContextBase<?>, TestProfile> blockingAuthGenerator(final AtomicInteger output) {
        return (context,profile)  -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            output.set(1);
            return profile;
        };
    }

}
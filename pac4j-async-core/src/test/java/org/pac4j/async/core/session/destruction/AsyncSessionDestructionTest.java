package org.pac4j.async.core.session.destruction;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for the standard AsyncSessionDestruction implementations
 */
public class AsyncSessionDestructionTest extends VertxAsyncTestBase {

    AsyncWebContext webContext;
    AsyncSessionStore sessionStore;

    @Before
    public void setUpWebContext() {
        webContext = mock(AsyncWebContext.class);
        sessionStore = mock(AsyncSessionStore.class);

        when(webContext.getSessionStore()).thenReturn(sessionStore);
        when(sessionStore.destroySession(eq(webContext))).thenReturn(CompletableFuture.completedFuture(true));
    }

    @Test
    public void testNoSessionDestruction(final TestContext testContext) {

        final Async async = testContext.async();
        final CompletableFuture<Void> sessionDestructionFuture = AsyncSessionDestruction.DO_NOT_DESTROY.attemptSessionDestructionFor(webContext);
        assertSuccessfulEvaluation(sessionDestructionFuture, v -> {
            verify(webContext, never()).getSessionStore();
            verify(sessionStore, never()).destroySession(any(AsyncWebContext.class));
        }, async);
    }

    @Test
    public void testWithSessionDestruction(final TestContext testContext) {

        final Async async = testContext.async();
        final CompletableFuture<Void> sessionDestructionFuture = AsyncSessionDestruction.DESTROY.attemptSessionDestructionFor(webContext);
        assertSuccessfulEvaluation(sessionDestructionFuture, v -> {
            verify(webContext, times(1)).getSessionStore();
            verify(sessionStore, times(1)).destroySession(any(AsyncWebContext.class));
        }, async);
    }

}
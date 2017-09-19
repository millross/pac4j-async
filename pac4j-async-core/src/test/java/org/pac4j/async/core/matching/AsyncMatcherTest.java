package org.pac4j.async.core.matching;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.stubbing.Answer;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.matching.Matcher;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test for conversion of sync to async matchers.
 */
public class AsyncMatcherTest extends VertxAsyncTestBase{

    public AsyncWebContext webContext;
    public Matcher<WebContext<?>> matcher = mock(Matcher.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setupWebContext() {
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter).build();
    }

    @Test(timeout=1000)
    public void testFromNonBlockingMatcherSuccessfulTrueEvaluation(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromNonBlocking(matcher);
        when(matcher.matches(webContext)).thenReturn(true);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res -> assertThat(res, is(true)), async);
    }

    @Test(timeout=1000)
    public void testFromNonBlockingMatcherSuccessfulFalseEvaluation(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromNonBlocking(matcher);
        when(matcher.matches(webContext)).thenReturn(false);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res -> assertThat(res, is(false)), async);
    }

    @Test(timeout=1000)
    public void testHttpActionExceptionBehaviour(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromNonBlocking(matcher);
        doAnswer((Answer<Boolean>) invocation -> {
            final HttpAction action = HttpAction.status("Intentional http action", 200, webContext);
            throw action;
        }).when(matcher).matches(eq(webContext));

        exception.expect(HttpAction.class);
        exception.expect(allOf(hasProperty("message", is("Intentional http action")),
                hasProperty("code", is(200))));
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }

    @Test(timeout=1000)
    public void testUnexpectedExceptionBehaviour(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromNonBlocking(matcher);
        doAnswer((Answer<Boolean>) invocation -> {
            throw new IntentionalException();
        }).when(matcher).matches(eq(webContext));
        exception.expect(IntentionalException.class);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }

    @Test(timeout=1000)
    public void testFromBlockingMatcherSuccessfulTrueEvaluation(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromBlocking(matcher);
        when(matcher.matches(webContext)).thenReturn(true);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res -> assertThat(res, is(true)), async);
    }

    @Test(timeout=1000)
    public void testFromBlockingMatcherSuccessfulFalseEvaluation(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromBlocking(matcher);
        when(matcher.matches(webContext)).thenReturn(false);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res -> assertThat(res, is(false)), async);
    }

    @Test(timeout=1000)
    public void testFromBlockingHttpActionExceptionBehaviour(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromBlocking(matcher);
        doAnswer((Answer<Boolean>) invocation -> {
            final HttpAction action = HttpAction.status("Intentional http action", 200, webContext);
            throw action;
        }).when(matcher).matches(eq(webContext));

        exception.expect(HttpAction.class);
        exception.expect(allOf(hasProperty("message", is("Intentional http action")),
                hasProperty("code", is(200))));
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }

    @Test(timeout=1000)
    public void testFromBlockingUnexpectedExceptionBehaviour(final TestContext testContext) throws Exception {
        final AsyncMatcher asyncMatcher = AsyncMatcher.fromBlocking(matcher);
        doAnswer((Answer<Boolean>) invocation -> {
            throw new IntentionalException();
        }).when(matcher).matches(eq(webContext));
        exception.expect(IntentionalException.class);
        final Async async = testContext.async();
        final CompletableFuture<Boolean> authFuture = asyncMatcher.matches(webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }
}
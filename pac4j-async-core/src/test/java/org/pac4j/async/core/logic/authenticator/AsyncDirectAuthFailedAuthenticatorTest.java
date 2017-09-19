package org.pac4j.async.core.logic.authenticator;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.client.AsyncDirectClient;
import org.pac4j.async.core.client.AsyncIndirectClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for the authenticator which will kick in if direct authentication fails.
 * This can have two possible outcomes:
 * (1) If it is possible to attempt indirect authentication for the requested endpoint, then this will be initiated
 * (2) If not, then authentication will fail and an unauthorized response will ensue
 */
public class AsyncDirectAuthFailedAuthenticatorTest extends VertxAsyncTestBase {

    private static final String AUTH_REDIRECT_URL = "http://example.com/redirect";
    private static final RedirectAction REDIRECT_ACTION = RedirectAction.redirect(AUTH_REDIRECT_URL);

    private final AsyncDirectAuthFailedAuthenticator<AsyncWebContext> authenticator = new AsyncDirectAuthFailedAuthenticator<>();
    private AsyncWebContext webContext = null;
    private Map<String, String> responseHeaders = null;

    @Before
    public void setUpWebContext() {
        responseHeaders = new HashMap<>();
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withRecordedResponseHeaders(responseHeaders)
                .build();
    }

    @Test
    public void indirectClientIsFirstClientAvailable(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<HttpAction> future = authenticator.authenticate(webContext, Arrays.asList(getIndirectClient()));
        final CompletableFuture<String> requestedUrlFuture = future.thenCompose(a -> {
            assertThat(a, is(notNullValue()));
            assertThat(a.getCode(), is(HttpConstants.TEMP_REDIRECT));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(AUTH_REDIRECT_URL));
            return webContext.getSessionStore().get(webContext, Pac4jConstants.REQUESTED_URL);
        });
        assertSuccessfulEvaluation(requestedUrlFuture, s -> {
            assertThat(s, is(MockAsyncWebContextBuilder.DEFAULT_FULL_REQUEST_URL));
        }, async);
    }

    @Test
    public void indirectClientIsSecondAvailable(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<HttpAction> future = authenticator.authenticate(webContext, Arrays.asList(getDirectClient(), getIndirectClient()));
        assertSuccessfulEvaluation(future, a -> {
            assertThat(a, is(notNullValue()));
            assertThat(a.getCode(), is(HttpConstants.UNAUTHORIZED));
            assertThat(a.getMessage(), is("unauthorized"));
        }, async);
    }

    @Test
    public void noClients(final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<HttpAction> future = authenticator.authenticate(webContext, Collections.emptyList());
        assertSuccessfulEvaluation(future, a -> {
            assertThat(a, is(notNullValue()));
            assertThat(a.getCode(), is(HttpConstants.UNAUTHORIZED));
            assertThat(a.getMessage(), is("unauthorized"));
        }, async);

    }

    private AsyncIndirectClient<TestCredentials, CommonProfile> getIndirectClient() {
        final AsyncIndirectClient<TestCredentials, CommonProfile> mockClient = mock(AsyncIndirectClient.class);
        when(mockClient.isIndirect()).thenReturn(true);
        doAnswer(invocation -> {
            final AsyncWebContext context = invocation.getArgumentAt(0, AsyncWebContext.class);
            return REDIRECT_ACTION.perform(context);
        }).when(mockClient).redirect(Matchers.any(AsyncWebContext.class));
        return mockClient;
    }

    private AsyncDirectClient<TestCredentials, CommonProfile> getDirectClient() {
        final AsyncDirectClient<TestCredentials, CommonProfile> mockClient = mock(AsyncDirectClient.class);
        when(mockClient.isIndirect()).thenReturn(false);
        return mockClient;
    }


}
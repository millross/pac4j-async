package org.pac4j.async.core.logic;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.mockito.Matchers;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.client.AsyncDirectClient;
import org.pac4j.async.core.client.AsyncIndirectClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.exception.handler.AsyncExceptionHandler.unwrapAsyncException;

/**
 * Test for the Async indirect authentication strategy, triggered when attempts to use direct auth have failed
 * and indirect clients are available to begin indirect authentication
 */
public class AsyncIndirectAuthenticationInitiatorTest extends VertxAsyncTestBase{

    private static final String AUTH_REDIRECT_URL = "http://example.com/redirect";
    private static RedirectAction REDIRECT_ACTION = RedirectAction.redirect(AUTH_REDIRECT_URL);

    private AsyncIndirectAuthenticationFlow<AsyncWebContext> authInitiator = new AsyncIndirectAuthenticationFlow<>();

    @Test(timeout = 2000)
    public void noIndirectClientsAvailable(final TestContext testContext) {
        final AsyncWebContext context = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .build();
        final List<AsyncClient<? extends Credentials, CommonProfile>> clients = Arrays.asList(getDirectClient());
        final Async async = testContext.async();
        authInitiator.initiateIndirectFlow(context, clients)
            .handle((a, t) -> {
                assertThat(t, is(notNullValue()));
                assertThat(a, is(nullValue()));
                final Throwable unwrapped = unwrapAsyncException(t);
                assertThat(unwrapped, instanceOf(TechnicalException.class));
                assertThat(unwrapped.getMessage(), is("No indirect client available for redirect"));
                async.complete();
                return null;
            });
    }

    @Test(timeout = 2000)
    public void indirectClientAvailable(final TestContext testContext) {

        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withRecordedResponseHeaders(responseHeaders)
                .build();
        final List<AsyncClient<? extends Credentials, CommonProfile>> clients = Arrays.asList(getIndirectClient());
        final Async async = testContext.async();
        authInitiator.initiateIndirectFlow(context, clients)
                .whenComplete((a, t) -> {
                    assertThat(t, is(nullValue()));
                    assertThat(a, is(notNullValue()));
                    assertThat(a.getCode(), is(HttpConstants.TEMP_REDIRECT));
                    assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(AUTH_REDIRECT_URL));
                })
                // Now validate session is as we expect
                .thenCompose(v -> context.getSessionStore().get(context, Pac4jConstants.REQUESTED_URL))
                .whenComplete((v, t) -> {
                    assertThat(t, is(nullValue()));
                    assertThat(v, is(MockAsyncWebContextBuilder.DEFAULT_FULL_REQUEST_URL));
                    async.complete();
                });

    }

    private AsyncDirectClient<TestCredentials, CommonProfile> getDirectClient() {
        final AsyncDirectClient<TestCredentials, CommonProfile> mockClient = mock(AsyncDirectClient.class);
        when(mockClient.isIndirect()).thenReturn(false);
        return mockClient;
    }

    private AsyncIndirectClient<TestCredentials, CommonProfile> getIndirectClient() {
        final AsyncIndirectClient<TestCredentials, CommonProfile> mockClient = mock(AsyncIndirectClient.class);
        when(mockClient.isIndirect()).thenReturn(true);
        doAnswer(invocation -> {
            final AsyncWebContext context = invocation.getArgumentAt(0, AsyncWebContext.class);
            return CompletableFuture.completedFuture(REDIRECT_ACTION.perform(context));
        }).when(mockClient).redirect(Matchers.any(AsyncWebContext.class));
        return mockClient;
    }
}
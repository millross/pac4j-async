package org.pac4j.async.core.authenticate;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Simple test for the client authentication wrapper.
 * Assumptions made based on existing code:-
 * getUserProfile with null credentials will return Optional.empty - our mock client will always enforce this
 * behaviour
 *
 */
public class AsyncClientAuthenticatorTest extends VertxAsyncTestBase{

    private static final String TEST_NAME = "testName";
    private static final String TEST_PASSWORD = "testPassword";
    private final static TestCredentials TEST_CREDENTIALS = new TestCredentials(TEST_NAME, TEST_PASSWORD);
    private static final TestProfile TEST_PROFILE = TestProfile.from(TEST_CREDENTIALS);
    final AsyncClientAuthenticator<CommonProfile, AsyncWebContext> authenticator = new AsyncClientAuthenticator<>();
    final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
    AsyncWebContext webContext = null;

    @Before
    public void setNullCredentialBehaviourInMock() {
        when(client.getUserProfileFuture(null, webContext)).thenReturn(delayedResult(() -> Optional.empty()));
    }

    @Before
    public void setupWebContext() {
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
    }

    @Test(timeout = 1000)
    public void testNonNullValidCredentials(final TestContext testContext) {
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> TEST_CREDENTIALS));
        when(client.getUserProfileFuture(TEST_CREDENTIALS, webContext)).thenReturn(delayedResult(() -> Optional.of(TEST_PROFILE)));
        final Async async = testContext.async();
        authenticator.authenticateFor(client, webContext)
            .thenAccept(o -> executionContext.runOnContext(() -> {
                assertThat(o.isPresent(), is(true));
                assertThat(o.get(), is(TEST_PROFILE));
                async.complete();
            }));
    }

    @Test
    public void testNullCredentialsReturned(final TestContext testContext) {
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> null));
        when(client.getUserProfileFuture(TEST_CREDENTIALS, webContext)).thenReturn(delayedResult(() -> Optional.of(TEST_PROFILE)));
        final Async async = testContext.async();
        authenticator.authenticateFor(client, webContext)
                .thenAccept(o -> executionContext.runOnContext(() -> {
                    assertThat(o.isPresent(), is(false));
                    async.complete();
                }));
    }

    @Test
    public void testEmptyProfileReturned(final TestContext testContext) {
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> TEST_CREDENTIALS));
        when(client.getUserProfileFuture(TEST_CREDENTIALS, webContext)).thenReturn(delayedResult(() -> Optional.empty()));
        final Async async = testContext.async();
        authenticator.authenticateFor(client, webContext)
                .thenAccept(o -> executionContext.runOnContext(() -> {
                    assertThat(o.isPresent(), is(false));
                    async.complete();
                }));
    }
}
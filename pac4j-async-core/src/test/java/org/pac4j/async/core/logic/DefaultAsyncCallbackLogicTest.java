package org.pac4j.async.core.logic;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.async.core.util.TestsConstants;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.util.TestsConstants.*;
import static org.pac4j.core.context.HttpConstants.LOCATION_HEADER;

/**
 * Replica of existing DefaultCallbackLogicTests for the async model
 */
public class DefaultAsyncCallbackLogicTest extends VertxAsyncTestBase {

    private AsyncWebContext webContext;
    private final AsyncConfig<Object, TestProfile, AsyncWebContext> config = mock(AsyncConfig.class);
    private final HttpActionAdapter<Object, AsyncWebContext> httpActionAdapter = mock(HttpActionAdapter.class);
    private DefaultAsyncCallbackLogic<Object, TestProfile, AsyncWebContext> asyncCallbackLogic;
    private AtomicInteger status;
    private Map<String, String> responseHeaders;
    private AsyncSessionStore sessionStore; // To determine whether renewSession was called

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        status = new AtomicInteger();
        responseHeaders = new HashMap<>();
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withStatusRecording(status)
                .withRecordedResponseHeaders(responseHeaders)
                .build();
        sessionStore = webContext.getSessionStore();
        when(sessionStore.renewSession(webContext)).thenReturn(CompletableFuture.completedFuture(true));
    }


    @Test
    public void testNullConfig() {
        exception.expect(TechnicalException.class);
        exception.expectMessage("config cannot be null");
        new DefaultAsyncCallbackLogic<>(false, false, null, httpActionAdapter);
    }

    @Test
    public void testNullHttpActionAdapter() {
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        exception.expect(TechnicalException.class);
        exception.expectMessage("httpActionAdapter cannot be null");
        new DefaultAsyncCallbackLogic<>(false, false, config, null);
    }

    @Test
    public void testNullWebContext() {
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        exception.expect(TechnicalException.class);
        exception.expectMessage("context cannot be null");
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
        asyncCallbackLogic.perform(null, null);
    }

    @Test
    public void testBlankDefaultUrl() {
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        exception.expect(TechnicalException.class);
        exception.expectMessage("defaultUrl cannot be blank");
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
        asyncCallbackLogic.perform(webContext, "");
    }

    @Test
    public void testNullClients() {
        when(config.getClients()).thenReturn(null);
        exception.expect(TechnicalException.class);
        exception.expectMessage("clients cannot be null");
        new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
    }

    @Test
    public void testDirectClient() throws Exception {

        final AsyncClient directClient = getMockDirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>(CALLBACK_URL, directClient);
        when(config.getClients()).thenReturn(clients);
        when(webContext.getRequestParameter(eq(Clients.DEFAULT_CLIENT_NAME_PARAMETER))).thenReturn(NAME);
        exception.expect(TechnicalException.class);
        exception.expectMessage("only indirect clients are allowed on the callback url");
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
        asyncCallbackLogic.perform(webContext, null);
    }

    @Test
    public void testCallback(final TestContext testContext) throws Exception {
        final TestProfile expectedProfile = TestProfile.from(TEST_CREDENTIALS);
        when(webContext.getRequestParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER)).thenReturn(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> future = asyncCallbackLogic.perform(webContext, null);
        final CompletableFuture<Map<String, TestProfile>> profilesFuture = future.thenAccept(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(LOCATION_HEADER), is(Pac4jConstants.DEFAULT_URL_VALUE));
            verify(sessionStore, never()).renewSession(any(AsyncWebContext.class));
        }).thenCompose((Void v) ->  webContext.getSessionStore().get(webContext, Pac4jConstants.USER_PROFILES));

        assertSuccessfulEvaluation(profilesFuture, profiles -> {
            assertThat(profiles.containsValue(expectedProfile), is(true));
            assertThat(profiles.size(), is(1));
        }, async);

    }

    @Test
    public void testCallbackWithOriginallyRequestedUrl(final TestContext testContext) throws Exception {
        final TestProfile expectedProfile = TestProfile.from(TEST_CREDENTIALS);
        when(webContext.getRequestParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER)).thenReturn(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        when(webContext.getRequestParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER)).thenReturn(NAME);
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> future = webContext.getSessionStore().set(webContext, Pac4jConstants.REQUESTED_URL, PAC4J_URL)
                .thenCompose(v -> asyncCallbackLogic.perform(webContext, null));

        final CompletableFuture<Map<String, TestProfile>> profilesFuture = future.thenAccept(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(LOCATION_HEADER), is(PAC4J_URL));
            verify(sessionStore, never()).renewSession(any(AsyncWebContext.class));
        }).thenCompose((Void v) ->  webContext.getSessionStore().get(webContext, Pac4jConstants.USER_PROFILES));

        assertSuccessfulEvaluation(profilesFuture, profiles -> {
            assertThat(profiles.containsValue(expectedProfile), is(true));
            assertThat(profiles.size(), is(1));
        }, async);
    }

    @Test
    public void testCallbackWithSessionRenewal(final TestContext testContext) throws Exception {
        final TestProfile expectedProfile = TestProfile.from(TEST_CREDENTIALS);
        when(webContext.getRequestParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER)).thenReturn(NAME);
        final String originalSessionId = UUID.randomUUID().toString();
        final String renewedSessionId = UUID.randomUUID().toString();
        when(sessionStore.getOrCreateSessionId(eq(webContext)))
                .thenReturn(CompletableFuture.completedFuture(originalSessionId))
                .thenReturn(CompletableFuture.completedFuture(renewedSessionId));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = clientsWithOneIndirectClient();
        when(config.getClients()).thenReturn(clients);
        asyncCallbackLogic = new DefaultAsyncCallbackLogic<>(false, true, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> future = asyncCallbackLogic.perform(webContext, null);
        final CompletableFuture<Map<String, TestProfile>> profilesFuture = future.thenAccept(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(LOCATION_HEADER), is(Pac4jConstants.DEFAULT_URL_VALUE));
            verify(sessionStore, times(1)).renewSession(any(AsyncWebContext.class));
        }).thenCompose((Void v) ->  webContext.getSessionStore().get(webContext, Pac4jConstants.USER_PROFILES));

        assertSuccessfulEvaluation(profilesFuture, profiles -> {
            assertThat(profiles.containsValue(expectedProfile), is(true));
            assertThat(profiles.size(), is(1));
        }, async);

    }


    private final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clientsWithOneIndirectClient() {
        final AsyncClient<TestCredentials, TestProfile> client = getMockIndirectClient(NAME);
        return new Clients<>(CALLBACK_URL, client);
    }


    private AsyncClient<TestCredentials, TestProfile> getMockDirectClient(final String name) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.getName()).thenReturn(name);
        when(client.isIndirect()).thenReturn(false);
        return client;
    }

    private AsyncClient<TestCredentials, TestProfile> getMockIndirectClient(final String name) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.getName()).thenReturn(name);
        doReturn(delayedResult(() -> TestsConstants.TEST_CREDENTIALS)).when(client).getCredentials(any(AsyncWebContext.class));
        when(client.isIndirect()).thenReturn(true);
        when(client.getUserProfileFuture(eq(TestsConstants.TEST_CREDENTIALS), any(AsyncWebContext.class)))
                .thenReturn(delayedResult(() -> Optional.of(TestProfile.from(TestsConstants.TEST_CREDENTIALS))));
        return client;
    }
}
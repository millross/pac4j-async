package org.pac4j.async.core.logic;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.stubbing.Answer;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.util.TestsConstants.*;

/**
 *
 */
public class DefaultAsyncLogoutLogicTest extends VertxAsyncTestBase {

    private final String DEFAULT_URL = "http://example.com";

    private AsyncWebContext webContext;
    private final AsyncConfig<Object, TestProfile, AsyncWebContext> config = mock(AsyncConfig.class);
    private final HttpActionAdapter<Object, AsyncWebContext> httpActionAdapter = mock(HttpActionAdapter.class);
    private DefaultAsyncLogoutLogic<Object, TestProfile, AsyncWebContext> asyncLogoutLogic;
    private AtomicInteger status;
    private StringBuffer buffer;
    private Map<String, String> responseHeaders;
    private AsyncSessionStore sessionStore; // To determine whether renewSession was called


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        status = new AtomicInteger();
        buffer = new StringBuffer();
        responseHeaders = new HashMap<>();
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withStatusRecording(status)
                .withResponseContentRecording(buffer)
                .withRecordedResponseHeaders(responseHeaders)
                .build();
        sessionStore = webContext.getSessionStore();
//        when(sessionStore.renewSession(webContext)).thenReturn(CompletableFuture.completedFuture(true));
    }

    @Test
    public void testNullConfig() {
        exception.expect(TechnicalException.class);
        exception.expectMessage("config cannot be null");

        new DefaultAsyncLogoutLogic<>(null, httpActionAdapter, null, null, false, false, false);
    }

    @Test
    public void testNullHttpActionAdapter() {
        exception.expect(TechnicalException.class);
        exception.expectMessage("httpActionAdapter cannot be null");
        when(config.getClients()).thenReturn(new Clients<>());

        new DefaultAsyncLogoutLogic<>(config, null, null, null, false, false, false);
    }

    @Test
    public void testBlankLogoutUrlPattern() {
        exception.expect(TechnicalException.class);
        exception.expectMessage("logoutUrlPattern cannot be blank");
        when(config.getClients()).thenReturn(new Clients<>());

        new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, "", false, false, false);
    }

    @Test
    public void testNullWebContext() {
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        exception.expect(TechnicalException.class);
        exception.expectMessage("context cannot be null");
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, null, false, false, false);
        asyncLogoutLogic.perform(null);
    }

    @Test
    public void testLogoutPerformed(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, null, true, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(200));
        }, async);
    }

    @Test
    public void testLogoutNotPerformedBecauseLocalLogoutIsFalse(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, null, false, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 1), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(200));
        }, async);
    }

    @Test
    public void testLogoutPerformedBecauseLocalLogoutIsFalseButMultipleProfiles(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        profiles.put(VALUE, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);


        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, null, false, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(200));
        }, async);
    }

    @Test
    public void testCentralLogout(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        final TestProfile profile = TestProfile.from(TEST_CREDENTIALS);
        profile.setClientName(NAME);
        profiles.put(NAME, profile);
        final AsyncClient<TestCredentials, TestProfile> client = indirectClient(NAME, CALLBACK_URL,
                targetUrl -> RedirectAction.redirect(CALLBACK_URL + "?p=" + targetUrl));

        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>(client);
        when(config.getClients()).thenReturn(clients);
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, ".*", true, false, true);
        when(webContext.getRequestParameter(eq(Pac4jConstants.URL))).thenReturn(CALLBACK_URL);
        final Async async = testContext.async();
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(CALLBACK_URL + "?p=" + CALLBACK_URL));
        }, async);
    }

    @Test
    public void testCentralLogoutWithRelativeUrl(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        final TestProfile profile = TestProfile.from(TEST_CREDENTIALS);
        profile.setClientName(NAME);
        profiles.put(NAME, profile);
        final AsyncClient<TestCredentials, TestProfile> client = indirectClient(NAME, PAC4J_BASE_URL, targetUrl -> RedirectAction.redirect(CALLBACK_URL + "?p=" + targetUrl));

        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>(client);
        when(config.getClients()).thenReturn(clients);
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, ".*", true, false, true);
        when(webContext.getRequestParameter(eq(Pac4jConstants.URL))).thenReturn(PATH);
        final Async async = testContext.async();
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(CALLBACK_URL + "?p=null"));
        }, async);

    }

    @Test
    public void testLogoutWithDefaultUrl(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, CALLBACK_URL, null, true, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(CALLBACK_URL));
        }, async);
    }

    @Test
    public void testLogoutWithGoodUrl(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        when(webContext.getRequestParameter(eq(Pac4jConstants.URL))).thenReturn(PATH);

        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, CALLBACK_URL, null, true, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(PATH));
        }, async);
    }

    @Test
    public void testLogoutWithBadUrlNoDefaultUrl(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        when(webContext.getRequestParameter(eq(Pac4jConstants.URL))).thenReturn(PATH);

        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, null, VALUE, true, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(200));
        }, async);

    }

    @Test
    public void testLogoutWithBadUrlButDefaultUrl(final TestContext testContext) {
        final Map<String, TestProfile> profiles = new HashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        final Clients<AsyncClient<? extends Credentials, ? extends TestProfile>, AsyncAuthorizationGenerator<TestProfile>> clients = new Clients<>();
        when(config.getClients()).thenReturn(clients);
        when(webContext.getRequestParameter(eq(Pac4jConstants.URL))).thenReturn(PATH);

        final Async async = testContext.async();
        asyncLogoutLogic = new DefaultAsyncLogoutLogic<>(config, httpActionAdapter, CALLBACK_URL, VALUE, true, false, false);
        final CompletableFuture<Object> resultFuture = addProfilesToContext(profiles)
                .thenCompose(v -> asyncLogoutLogic.perform(webContext));

        assertSuccessfulEvaluation(assertProfilesCount(resultFuture, 0), o -> {
            assertThat(buffer.toString(), is(""));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(CALLBACK_URL));
        }, async);

    }

    public CompletableFuture<Map<String, TestProfile>> profilesFromSession() {
        return webContext.getSessionStore().get(webContext, Pac4jConstants.USER_PROFILES);
    }

    public Map<String, TestProfile> profilesFromRequest() {
        return (Map<String, TestProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
    }

    private CompletableFuture<Void> assertProfilesCount(final CompletableFuture<Object> resultFuture, final int expectedSize) {
        return resultFuture
                .thenCompose(o -> {
                    assertThat(profilesFromRequest().size(), is(expectedSize));
                    return profilesFromSession();
                })
                .thenAccept(profilesFromSession -> {
                    assertThat(profilesFromSession.size(), is(expectedSize));
                });
    }

    private CompletableFuture<Void> addProfilesToContext(final Map<String, ? extends CommonProfile> profiles) {
        webContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profiles);
        return webContext.getSessionStore().set(webContext, Pac4jConstants.USER_PROFILES, profiles);
    }

    private AsyncClient<TestCredentials, TestProfile> indirectClient(final String name, final String callbackUrl, final Function<String, RedirectAction> redirectActionGenerator) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.isIndirect()).thenReturn(true);
        when(client.getName()).thenReturn(name);
        doAnswer((Answer<RedirectAction>) invocation -> {
            final String targetUrl = invocation.getArgumentAt(2, String.class);
            System.out.println("*" + targetUrl);
            return redirectActionGenerator.apply(targetUrl);
        }).when(client).getLogoutAction(any(AsyncWebContext.class), any(TestProfile.class), any(String.class));
        return client;
    }

}
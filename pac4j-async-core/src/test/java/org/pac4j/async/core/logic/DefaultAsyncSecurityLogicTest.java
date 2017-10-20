package org.pac4j.async.core.logic;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.matching.AsyncMatcher;
import org.pac4j.async.core.util.TestsConstants;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.util.TestsConstants.*;
import static org.pac4j.core.context.HttpConstants.LOCATION_HEADER;

/**
 * Tests for DefaultAsyncSecurityLogic. As far as possible these should mimic the sync versions of the same tests
 */
public class DefaultAsyncSecurityLogicTest extends VertxAsyncTestBase {

    private DefaultAsyncSecurityLogic<Object, CommonProfile, AsyncWebContext> asyncSecurityLogic;
    private final AsyncConfig<Object, CommonProfile, AsyncWebContext> config = mock(AsyncConfig.class);
    private final HttpActionAdapter<Object, AsyncWebContext> httpActionAdapter = mock(HttpActionAdapter.class);
    private final SecurityGrantedAccessAdapter<Object, AsyncWebContext> accessGrantedAdapter = mock(SecurityGrantedAccessAdapter.class);

    private AtomicInteger status;
    private AsyncWebContext webContext;
    private Map<String, String> responseHeaders;

    @Before
    public void setUp() {
        status = new AtomicInteger();
        responseHeaders = new HashMap<>();
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withStatusRecording(status)
                .withRecordedResponseHeaders(responseHeaders)
                .build();

    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullConfig() throws Exception {
        exception.expect(TechnicalException.class);
        exception.expectMessage("config cannot be null");
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, null, null);
    }

    @Test
    public void testNullContext() throws Exception {
        when(config.getClients()).thenReturn(new Clients());
        asyncSecurityLogic = new DefaultAsyncSecurityLogic <>(true, false, config, httpActionAdapter);
        exception.expect(TechnicalException.class);
        exception.expectMessage("context cannot be null");
        asyncSecurityLogic.perform(null, accessGrantedAdapter, null, null, null);
    }

    @Test
    public void testNullClients() throws Exception {
        // Config.getClients() will return null
        exception.expect(TechnicalException.class);
        exception.expectMessage("configClients cannot be null");
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
    }

    @Test
    public void testNullHttpActionAdapter() throws Exception {
        when(config.getClients()).thenReturn(new Clients());
        exception.expect(TechnicalException.class);
        exception.expectMessage("httpActionAdapter cannot be null");
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, null);
    }

    @Test
    public void testNotAuthenticated(final TestContext testContext) throws Exception {
        final AsyncClient<TestCredentials, TestProfile> client = getMockIndirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, client);
        when(config.getClients()).thenReturn(clients);
        final Async async = testContext.async();
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);

        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, null, null, null);

        assertSuccessfulEvaluation(result, o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(401));
        }, async);
    }

    @Test
    public void testNotAuthenticatedButMatcher(final TestContext testContext) throws Exception {
        final AsyncClient<TestCredentials, TestProfile> client = getMockIndirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, client);
        when(config.getClients()).thenReturn(clients);
        final Map<String, AsyncMatcher> matchers = new HashMap<>();
        matchers.put(NAME, AsyncMatcher.fromNonBlocking(context -> false));
        when(config.getMatchers()).thenReturn(matchers);
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, null, null, NAME);

        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(-1));
            verify(accessGrantedAdapter, times(1)).adapt(webContext);
        }), async);
    }

    @Test
    public void testAlreadyAuthenticatedAndAuthorized(final TestContext testContext) throws Exception {
        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient);
        when(config.getClients()).thenReturn(clients);
        final String authorizers = NAME;
        addSingleAuthorizerToConfig((context, prof) -> prof.get(0).getId().equals(GOOD_USERNAME));
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = simulatePreviousAuthenticationSuccess()
            .thenCompose(v -> asyncSecurityLogic.perform(webContext, accessGrantedAdapter, null, authorizers, null));
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(-1));
            verify(accessGrantedAdapter, times(1)).adapt(webContext);
        }), async);
    }

    @Test(timeout = 1000)
    public void testAlreadyAuthenticatedNotAuthorized(final TestContext testContext) throws Exception {
        simulatePreviousAuthenticationSuccess();
        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient);
        when(config.getClients()).thenReturn(clients);
        final String authorizers = NAME;
        addSingleAuthorizerToConfig((context, prof) -> prof.get(0).getId().equals(BAD_USERNAME));
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = simulatePreviousAuthenticationSuccess()
                .thenCompose(v -> asyncSecurityLogic.perform(webContext, accessGrantedAdapter, null, authorizers, null));

        exception.expect(CompletionException.class);
        exception.expectCause(allOf(IsInstanceOf.instanceOf(HttpAction.class),
                hasProperty("message", is("forbidden")),
                hasProperty("code", is(403))));
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(403));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);
    }

    @Test(timeout = 1000)
    public void testAuthorizerThrowsRequiresHttpAction(final TestContext testContext) throws Exception {
        simulatePreviousAuthenticationSuccess();
        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient);
        when(config.getClients()).thenReturn(clients);
        final String authorizers = NAME;
        addSingleAuthorizerToConfig((context, prof) -> { throw HttpAction.status("bad request", 400, context); });
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = simulatePreviousAuthenticationSuccess()
                .thenCompose(v -> asyncSecurityLogic.perform(webContext, accessGrantedAdapter, null, authorizers, null));
        exception.expect(CompletionException.class);
        exception.expectCause(allOf(IsInstanceOf.instanceOf(HttpAction.class),
                hasProperty("message", is("bad request")),
                hasProperty("code", is(400))));
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(400));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);
    }

    @Test
    public void testDoubleDirectClient(final TestContext testContext) throws Exception {

        final TestProfile profile = TestProfile.from(TEST_CREDENTIALS);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients =
            doubleDirectClients();
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME + "," + VALUE;
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertSuccessfulLoginWithProfiles(result, async, profile);
    }

    @Test
    public void testDirectClientThrowsRequiresHttpAction(final TestContext testContext) throws Exception {
        final AsyncClient directClient = getMockDirectClient(NAME, TEST_CREDENTIALS);
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, directClient);
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME;
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        when(directClient.getCredentials(eq(webContext))).thenReturn(delayedException(250,
                (() -> HttpAction.status("bad request", 400, webContext))));
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        exception.expect(CompletionException.class);
        exception.expectCause(allOf(IsInstanceOf.instanceOf(HttpAction.class),
                hasProperty("message", is("bad request")),
                hasProperty("code", is(400))));

        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(400));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);
    }

    @Test
    public void testDoubleDirectClientSupportingMultiProfile(final TestContext testContext) throws Exception {
        final TestProfile profile = TestProfile.from(TEST_CREDENTIALS);
        final TestProfile profile2 = TestProfile.from(new TestCredentials(GOOD_USERNAME2, PASSWORD));
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = doubleDirectClients();
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME + "," + VALUE;
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, true, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertSuccessfulLoginWithProfiles(result, async, profile, profile2);
    }

    @Test
    public void testDoubleDirectClientChooseDirectClient(final TestContext testContext) throws Exception {
        final TestProfile profile2 = TestProfile.from(new TestCredentials(GOOD_USERNAME2, PASSWORD));
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = doubleDirectClients();
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME + "," + VALUE;
        when(webContext.getRequestParameter(eq(Clients.DEFAULT_CLIENT_NAME_PARAMETER))).thenReturn(VALUE);
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, true, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertSuccessfulLoginWithProfiles(result, async, profile2);
    }

    @Test
    public void testDoubleDirectClientChooseBadDirectClient(final TestContext testContext) throws Exception {
        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = doubleDirectClients();
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME;
        when(webContext.getRequestParameter(eq(Clients.DEFAULT_CLIENT_NAME_PARAMETER))).thenReturn(VALUE);
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, true, config, httpActionAdapter);
        final Async async = testContext.async();

        exception.expect(CompletionException.class);
        exception.expectCause(allOf(IsInstanceOf.instanceOf(TechnicalException.class),
                hasProperty("message", is("Client not allowed: " + VALUE))));
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);

    }

    @Test
    public void testRedirectByIndirectClient(final TestContext testContext) throws Exception {

        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME, PAC4J_URL);

        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient);
        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME;
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertRedirectionTo(async, result, PAC4J_URL);
    }

    @Test
    public void testDoubleIndirectClientOneChosen(final TestContext testContext) throws Exception {

        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME, PAC4J_URL);
        final AsyncClient<TestCredentials, TestProfile> indirectClient2 = getMockIndirectClient(VALUE, PAC4J_BASE_URL);

        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient, indirectClient2);

        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME + "," + VALUE;
        when(webContext.getRequestParameter(eq(Clients.DEFAULT_CLIENT_NAME_PARAMETER))).thenReturn(VALUE);
        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);
        final Async async = testContext.async();
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertRedirectionTo(async, result, PAC4J_BASE_URL);
    }

    @Test
    public void testDoubleIndirectClientBadOneChosen(final TestContext testContext) throws Exception {
        final AsyncClient<TestCredentials, TestProfile> indirectClient = getMockIndirectClient(NAME, PAC4J_URL);
        final AsyncClient<TestCredentials, TestProfile> indirectClient2 = getMockIndirectClient(VALUE, PAC4J_BASE_URL);

        final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(CALLBACK_URL, indirectClient, indirectClient2);

        when(config.getClients()).thenReturn(clients);
        final String clientNames = NAME;
        when(webContext.getRequestParameter(eq(Clients.DEFAULT_CLIENT_NAME_PARAMETER))).thenReturn(VALUE);

        asyncSecurityLogic = new DefaultAsyncSecurityLogic<>(true, false, config, httpActionAdapter);

        final Async async = testContext.async();

        exception.expect(CompletionException.class);
        exception.expectCause(allOf(IsInstanceOf.instanceOf(TechnicalException.class),
                hasProperty("message", is("Client not allowed: " + VALUE))));
        final CompletableFuture<Object> result = asyncSecurityLogic.perform(webContext, accessGrantedAdapter, clientNames, null, null);
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);
    }

    private void assertRedirectionTo(Async async, CompletableFuture<Object> result, final String url) {
        assertSuccessfulEvaluation(result, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(302));
            assertThat(responseHeaders.get(LOCATION_HEADER), is(url));
            verify(accessGrantedAdapter, times(0)).adapt(webContext);
        }), async);
    }

    private CompletableFuture<Void> assertSuccessfulLoginWithProfiles(final CompletableFuture<Object> future,
                                                                      final Async async,
                                                                      final TestProfile ... expectedProfiles) {
        return assertSuccessfulEvaluation(future, ExceptionSoftener.softenConsumer(o -> {
            assertThat(o, is(nullValue()));
            assertThat(status.get(), is(-1));

            verify(accessGrantedAdapter, times(1)).adapt(webContext);
            final LinkedHashMap<String, TestProfile> profiles = (LinkedHashMap<String, TestProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
            assertThat(profiles.size(), is(profiles.size()));
            Arrays.stream(expectedProfiles).forEach(profile -> {
                assertThat(profiles.values(), hasItem(profile));
            });

        }), async);

    }

    private Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> doubleDirectClients() {
        final TestCredentials testCredentials2 = new TestCredentials(GOOD_USERNAME2, PASSWORD);

        final AsyncClient directClient = getMockDirectClient(NAME, TEST_CREDENTIALS);
        final AsyncClient directClient2 = getMockDirectClient(VALUE, testCredentials2);
        return new Clients<>(CALLBACK_URL, directClient, directClient2);
    }

    private void addSingleAuthorizerToConfig(Authorizer<WebContext, CommonProfile> syncAuthorizer) {
        final Map<String, AsyncAuthorizer<CommonProfile>> authorizersMap = new HashMap<>();
        authorizersMap.put(NAME, AsyncAuthorizer.fromNonBlockingAuthorizer(syncAuthorizer));
        when(config.getAuthorizers()).thenReturn(authorizersMap);
    }

    private CompletableFuture<Void> simulatePreviousAuthenticationSuccess() {
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, TestProfile.from(TEST_CREDENTIALS));
        return webContext.getSessionStore().set(webContext, Pac4jConstants.USER_PROFILES, profiles);
    }

    private AsyncClient<TestCredentials, TestProfile> getMockIndirectClient(final String name, final String redirectUrl) {
        final AsyncClient<TestCredentials, TestProfile> client = getMockIndirectClient(name);
        Mockito.doAnswer(invodation -> {
            final RedirectAction redirectAction = RedirectAction.redirect(redirectUrl);
            return CompletableFuture.completedFuture(redirectAction.perform(webContext));
        }).when(client).redirect(eq(webContext));
        return client;
    }

    private AsyncClient<TestCredentials, TestProfile> getMockIndirectClient(final String name) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.getName()).thenReturn(name);
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> TestsConstants.TEST_CREDENTIALS));
        when(client.isIndirect()).thenReturn(true);
        when(client.getUserProfileFuture(eq(TestsConstants.TEST_CREDENTIALS), any(AsyncWebContext.class)))
                .thenReturn(delayedResult(() -> Optional.of(TestProfile.from(TestsConstants.TEST_CREDENTIALS))));
        return client;
    }

    private AsyncClient<TestCredentials, TestProfile> getMockDirectClient(final String name,
                                                                                final TestCredentials credentials) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.getName()).thenReturn(name);
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> credentials));
        when(client.getUserProfileFuture(eq(credentials), any(AsyncWebContext.class)))
                .thenReturn(delayedResult(() -> {
                    final TestProfile profile = TestProfile.from(credentials);
                    profile.setClientName(name);
                    return Optional.of(profile);
                }));
        return client;
    }

}
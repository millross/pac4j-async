package org.pac4j.async.core.logic.authenticator;

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
import org.pac4j.async.core.logic.decision.AsyncLoadProfileFromSessionDecision;
import org.pac4j.async.core.logic.decision.AsyncSaveProfileToSessionDecision;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.profile.save.AsyncProfileSave;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class AsyncDirectClientAuthenticatorTest extends VertxAsyncTestBase {

    private static final String TEST_NAME = "testName";
    private static final String TEST_PASSWORD = "testPassword";
    private final static TestCredentials TEST_CREDENTIALS = new TestCredentials(TEST_NAME, TEST_PASSWORD);
    private static final TestProfile TEST_PROFILE = TestProfile.from(TEST_CREDENTIALS);
    private static final TestProfile TEST_PROFILE2 = TestProfile.from(TEST_CREDENTIALS);

    static {
        TEST_PROFILE.setClientName("client1");
        TEST_PROFILE2.setClientName("client2");
    }

//    Can't do this here
    private MockAsyncWebContextBuilder webContextBuilder;

    @Before
    public void setupClient() {
        webContextBuilder = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter);
    }

    @Test
    public void noClientsResultsInNoProfiles(final TestContext testContext) {

        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.SINGLE_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(true), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final Async async = testContext.async();

        authenticator.authenticate(new ArrayList<>(), webContext, new AsyncProfileManager<>(webContext))
            .thenAccept(profiles -> executionContext.runOnContext(() -> {
                assertThat(profiles, is(new ArrayList<>()));
                async.complete();
            }));

    }

    @Test(timeout = 1000)
    public void noDirectClientsResultsInNoProfiles(final TestContext testContext) {

        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.SINGLE_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(true), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final AsyncClient<TestCredentials, TestProfile> indirectClient = getClient(true);
        final Async async = testContext.async();

        final CompletableFuture<List<TestProfile>>authResultFuture = authenticator.authenticate(Arrays.asList(indirectClient), webContext, new AsyncProfileManager<>(webContext));

        assertSuccessfulEvaluation(authResultFuture,
                profiles -> assertThat(profiles, is(new ArrayList<>())),
                async);

    }

    @Test
    public void noClientsSuccessfullyAuthenticate(final TestContext testContext) {
        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.SINGLE_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(true), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final AsyncClient<TestCredentials, TestProfile> directClient = getClient(false, false);
        final Async async = testContext.async();

        final CompletableFuture<List<TestProfile>>authResultFuture = authenticator.authenticate(Arrays.asList(directClient), webContext, new AsyncProfileManager<>(webContext));

        assertSuccessfulEvaluation(authResultFuture,
                profiles -> assertThat(profiles, is(new ArrayList<>())),
                async);

    }

    @Test
    public void withoutSavingToSession(final TestContext testContext) {
        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.SINGLE_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(false), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final AsyncClient<TestCredentials, TestProfile> directClient = getClient(false);
        final Async async = testContext.async();

        final CompletableFuture<List<TestProfile>>authResultFuture = authenticator.authenticate(Arrays.asList(directClient), webContext, new AsyncProfileManager<>(webContext));

        assertSuccessfulEvaluation(authResultFuture,
                profiles -> assertThat(profiles, is(Arrays.asList(TEST_PROFILE))),
                async);

    }

    @Test
    public void withSavingToSession(final TestContext testContext) {
        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.SINGLE_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(true), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final AsyncClient<TestCredentials, TestProfile> directClient = getClient(false);
        final Async async = testContext.async();

        final CompletableFuture<List<TestProfile>>authResultFuture = authenticator.authenticate(Arrays.asList(directClient), webContext, new AsyncProfileManager<>(webContext));

        assertSuccessfulEvaluation(authResultFuture,
                profiles -> assertThat(profiles, is(Arrays.asList(TEST_PROFILE))),
                async);

    }

    @Test
    public void multiProfileSavingToSession(final TestContext testContext) {
        final AsyncDirectClientAuthenticator<TestProfile, AsyncWebContext> authenticator = new AsyncDirectClientAuthenticator<>(AsyncProfileSave.MULTI_PROFILE_SAVE,
                new AsyncSaveProfileToSessionDecision(true), new AsyncLoadProfileFromSessionDecision());
        final AsyncWebContext webContext = webContextBuilder.build();
        final AsyncClient<TestCredentials, TestProfile> directClient1 = getDirectClient(TEST_PROFILE);
        when(directClient1.getName()).thenReturn("testClient1");
        when(directClient1.getName()).thenReturn("testClient2");
        final AsyncClient<TestCredentials, TestProfile> directClient2 = getDirectClient(TEST_PROFILE2);
        final Async async = testContext.async();

        final CompletableFuture<List<TestProfile>>authResultFuture = authenticator.authenticate(Arrays.asList(directClient1, directClient2), webContext, new AsyncProfileManager<>(webContext));

        assertSuccessfulEvaluation(authResultFuture,
                profiles -> assertThat(profiles, is(Arrays.asList(TEST_PROFILE, TEST_PROFILE2))),
                async);

    }

    /**
     * Return a client which will successfully authenticate
     * @param indirect
     * @return
     */
    private AsyncClient<TestCredentials, TestProfile> getClient(final boolean indirect) {
        return getClient(indirect, true);
    }

    private AsyncClient<TestCredentials, TestProfile> getClient(final boolean indirect, final boolean willAuthenticate) {
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.isIndirect()).thenReturn(indirect);
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> TEST_CREDENTIALS));
        when(client.getUserProfileFuture(eq(TEST_CREDENTIALS), any(AsyncWebContext.class))).thenReturn(
                delayedResult(() -> Optional.ofNullable(willAuthenticate ? TEST_PROFILE : null)));
        return client;
    }

    private AsyncClient<TestCredentials, TestProfile> getDirectClient(final TestProfile profileToReturn) {
        Objects.requireNonNull(profileToReturn);
        final AsyncClient<TestCredentials, TestProfile> client = mock(AsyncClient.class);
        when(client.isIndirect()).thenReturn(false);
        when(client.getCredentials(any(AsyncWebContext.class))).thenReturn(delayedResult(() -> TEST_CREDENTIALS));
        when(client.getUserProfileFuture(eq(TEST_CREDENTIALS), any(AsyncWebContext.class))).thenReturn(
                delayedResult(() -> Optional.of(profileToReturn)));
        return client;
    }
}
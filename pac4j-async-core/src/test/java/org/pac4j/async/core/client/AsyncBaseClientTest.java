package org.pac4j.async.core.client;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.*;
import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuthenticationStrategy;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.exception.handler.AsyncExceptionHandler;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class AsyncBaseClientTest  extends VertxAsyncTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncBaseClientTest.class);

    protected static final String HAPPY_PATH_NAME = "happyTestUser";
    protected static final String HAPPY_PATH_PASSWORD = "happyPathPassword";
    protected static final String PERMISSION_ADMIN = "admin";
    protected static final String PERMISSION_SYSTEM = "system";

    private AsyncWebContext webContext;

    @Before
    public void setupWebContext() {
        // We don't have an execution context till this point so we need to initialise here
        webContext =  MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter).build();
    }

    @Test(timeout = 1000)
    public void testGetProfileWithNoAuthenticators(final TestContext testContext) {

        final Async async = testContext.async();

        final AsyncClient<TestCredentials, TestProfile> client = happyPathClient();
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, webContext);

        profileFuture.thenAccept(o -> o.ifPresent(p -> {
            rule.vertx().runOnContext(v -> {
                assertThat(p.getId(), is(HAPPY_PATH_NAME));
                async.complete();
            });
        }));
    }

    @Test(timeout = 1000)
    public void testGetProfileSuccessfullyWithAuthenticators(final TestContext testContext) {
        final Async async = testContext.async();

        final AsyncBaseClient<TestCredentials, TestProfile> client = happyPathClient();

        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_ADMIN));
        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_SYSTEM));
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, webContext);

        profileFuture.thenAccept(o -> o.ifPresent(p -> {
            LOG.debug("Profile future completed " + System.currentTimeMillis());
            rule.vertx().runOnContext(v -> {
                assertThat(p.getId(), is(HAPPY_PATH_NAME));
                assertThat(p.getPermissions().size(), is(2));
                assertThat(p.getPermissions(), is(new HashSet(Arrays.asList(PERMISSION_ADMIN, PERMISSION_SYSTEM))));
                async.complete();
            });
        }));

    }

    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testFailingGetProfileWithNoAuthenticators(final TestContext testContext) {

        final Async async = testContext.async();

        final AsyncClient<TestCredentials, TestProfile> client = failingRetrievalClient();
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, webContext);

        expectIntentionalFailureOf(profileFuture);
    }


    @Test(timeout = 1000,  expected = IntentionalException.class)
    public void testGetProfileSuccessfullyWithFailingAuthGenerator(final TestContext testContext) {
        final Async async = testContext.async();

        final AsyncBaseClient<TestCredentials, TestProfile> client = happyPathClient();

        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_ADMIN));
        client.addAuthorizationGenerator(failingPermissionAuthGenerator());
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, null);

        expectIntentionalFailureOf(profileFuture);

    }

    @Test(timeout = 1000,  expected = IntentionalException.class)
    public void testGetProfileSuccessfullyWithFailingProfileModifier(final TestContext testContext) {
        testContext.async();

        final AsyncBaseClient<TestCredentials, TestProfile> client = happyPathClient();

        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_ADMIN));
        client.addAuthorizationGenerator(failingProfileModifierGenerator());
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, webContext);

        expectIntentionalFailureOf(profileFuture);

    }

    @Test(timeout = 1000)
    public void testEmptyProfileOptionalPropagatesCorrectly(final TestContext testContext) {
        final Async async = testContext.async();

        final AsyncBaseClient<TestCredentials, TestProfile> client = emptyProfileClient();

        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_ADMIN));
        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_SYSTEM));
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<Optional<TestProfile>> profileFuture = client.getUserProfileFuture(credentials, webContext);

        profileFuture.thenAccept(o -> rule.vertx().runOnContext(v -> {
            assertThat(o.isPresent(), is(false));
            async.complete();
        }));

    }

    private AsyncAuthorizationGenerator<TestProfile> failingProfileModifierGenerator() {
        return successfulAuthGenerator(failingProfileModifier());
    }

    private Consumer<TestProfile> failingProfileModifier() {
        return p -> {
            throw new IntentionalException();
        };
    }


    private void expectIntentionalFailureOf(CompletableFuture<Optional<TestProfile>> profileFuture) {
        profileFuture.whenComplete((p, t) -> {
            if (p != null) {
                executionContext.runOnContext(() -> {
                    throw new RuntimeException("profile should be null");
                });
            } else {
                AsyncExceptionHandler.handleException(t, e -> {
                    // Note implication of use of completeExceptionally
                    if (e instanceof IntentionalException) {
                        executionContext.runOnContext(() -> {
                            throw (IntentionalException) e;
                        });
                    }
                    throw new RuntimeException(e);
                });
            }
        });
    }

    private final AsyncAuthorizationGenerator<TestProfile> successfulPermissionAuthGenerator(final String permissionName) {
        return successfulAuthGenerator(profileToDecorate -> profileToDecorate.addPermission(permissionName));
    }

    private final AsyncAuthorizationGenerator<TestProfile> successfulAuthGenerator(final Consumer<TestProfile> profileModifier) {
        return (context, profile) -> {
            LOG.info("Starting processing for profile");
            final CompletableFuture<Consumer<TestProfile>> future = new CompletableFuture<>();
            rule.vertx().setTimer(300, l -> {
                LOG.info("Completing profile decorator");
                future.complete(profileModifier);
            });
            return future;
        };
    }

    private final AsyncAuthorizationGenerator<TestProfile> failingPermissionAuthGenerator() {
        return (context, profile) -> {
            LOG.info("Starting failing permission generator processing for profile");
            final CompletableFuture<Consumer<TestProfile>> future = new CompletableFuture<>();
            rule.vertx().setTimer(300, l -> {
                LOG.info("Completing failing profile decorator");
                future.completeExceptionally(new IntentionalException());
            });
            return future;
        };
    }


    private AsyncBaseClient<TestCredentials, TestProfile> happyPathClient() {

        return new AsyncBaseClient<TestCredentials, TestProfile>() {

            @Override
            public boolean isIndirect() {
                return false;
            }

            @Override
            protected CompletableFuture<Optional<TestProfile>> retrieveUserProfileFuture(TestCredentials credentials, AsyncWebContext context) {
                final CompletableFuture<Optional<TestProfile>> future = new CompletableFuture<>();
                rule.vertx().setTimer(300, l -> future.complete(Optional.of(TestProfile.from(credentials))));
                return future;
            }

            @Override
            protected RecordFailedAuthenticationStrategy authFailureRecorder() {
                return null;
            }

            @Override
            public CompletableFuture<HttpAction> redirect(AsyncWebContext context) {
                return null;
            }

            @Override
            public RedirectAction getLogoutAction(AsyncWebContext var1, TestProfile var2, String var3) {
                return null;
            }

            @Override
            protected void internalInit(AsyncWebContext context) {

            }
        };
    }

    private AsyncBaseClient<TestCredentials, TestProfile> emptyProfileClient() {
        return new AsyncBaseClient<TestCredentials, TestProfile>() {

            @Override
            public boolean isIndirect() {
                return false;
            }

            @Override
            protected CompletableFuture<Optional<TestProfile>> retrieveUserProfileFuture(TestCredentials credentials, AsyncWebContext context) {
                final CompletableFuture<Optional<TestProfile>> future = new CompletableFuture<>();
                rule.vertx().setTimer(300, l -> future.complete(Optional.empty()));
                return future;
            }

            @Override
            protected RecordFailedAuthenticationStrategy authFailureRecorder() {
                return null;
            }

            @Override
            public CompletableFuture<HttpAction> redirect(AsyncWebContext context) {
                return null;
            }

            @Override
            public RedirectAction getLogoutAction(AsyncWebContext var1, TestProfile var2, String var3) {
                return null;
            }

            @Override
            protected void internalInit(final AsyncWebContext context) {

            }
        };
    }

    private AsyncBaseClient<TestCredentials, TestProfile> failingRetrievalClient() {
        return new AsyncBaseClient<TestCredentials, TestProfile>() {

            @Override
            public boolean isIndirect() {
                return false;
            }

            @Override
            protected CompletableFuture<Optional<TestProfile>> retrieveUserProfileFuture(TestCredentials credentials, AsyncWebContext context) {
                final CompletableFuture<Optional<TestProfile>> future = new CompletableFuture<>();
                rule.vertx().setTimer(300, l -> {
                    future.completeExceptionally(new IntentionalException());
                });
                return future;
            }

            @Override
            protected RecordFailedAuthenticationStrategy authFailureRecorder() {
                return null;
            }

            @Override
            public CompletableFuture<HttpAction> redirect(AsyncWebContext context) {
                return null;
            }

            @Override
            public RedirectAction getLogoutAction(AsyncWebContext var1, TestProfile var2, String var3) {
                return null;
            }

            @Override
            protected void internalInit(final AsyncWebContext context) {

            }
        };
    }


}
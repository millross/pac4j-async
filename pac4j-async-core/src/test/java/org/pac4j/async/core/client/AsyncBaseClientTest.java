package org.pac4j.async.core.client;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.exception.handler.AsyncExceptionHandler;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Arrays;
import java.util.HashSet;
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

    @Test(timeout = 1000)
    public void testGetProfileWithNoAuthenticators(final TestContext testContext) {

        final Async async = testContext.async();

        final AsyncClient<TestCredentials, TestProfile> client = happyPathClient();
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<TestProfile> profileFuture = client.getUserProfileFuture(credentials, null);

        profileFuture.thenAccept(p -> {
            rule.vertx().runOnContext(v -> {
                assertThat(p.getId(), is(HAPPY_PATH_NAME));
                async.complete();
            });
        });
    }

    @Test(timeout = 1000)
    public void testGetProfileSuccessfullyWithAuthenticators(final TestContext testContext) {
        final Async async = testContext.async();

        final AsyncBaseClient<TestCredentials, TestProfile> client = happyPathClient();

        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_ADMIN));
        client.addAuthorizationGenerator(successfulPermissionAuthGenerator(PERMISSION_SYSTEM));
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<TestProfile> profileFuture = client.getUserProfileFuture(credentials, null);

        profileFuture.thenAccept(p -> {
            LOG.debug("Profile future completed " + System.currentTimeMillis());
            rule.vertx().runOnContext(v -> {
                assertThat(p.getId(), is(HAPPY_PATH_NAME));
                assertThat(p.getPermissions().size(), is(2));
                assertThat(p.getPermissions(), is(new HashSet(Arrays.asList(PERMISSION_ADMIN, PERMISSION_SYSTEM))));
                async.complete();
            });
        });

    }

    @Test(timeout = 1000, expected = IntentionalException.class)
    public void testFailingGetProfileWithNoAuthenticators(final TestContext testContext) {

        final Async async = testContext.async();

        final AsyncClient<TestCredentials, TestProfile> client = failingRetrievalClient();
        final TestCredentials credentials = new TestCredentials(HAPPY_PATH_NAME, HAPPY_PATH_PASSWORD);

        // We don't care about web contexts right now
        final CompletableFuture<TestProfile> profileFuture = client.getUserProfileFuture(credentials, null);

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
        final CompletableFuture<TestProfile> profileFuture = client.getUserProfileFuture(credentials, null);

        expectIntentionalFailureOf(profileFuture);

    }

    protected void expectIntentionalFailureOf(CompletableFuture<TestProfile> profileFuture) {
        profileFuture.whenComplete((p, t) -> {
            if (p != null) {
                contextRunner.runOnContext(() -> {
                    throw new RuntimeException("profile should be null");
                });
            } else {
                AsyncExceptionHandler.handleException(t, e -> {
                    // Note implication of use of completeExceptionally
                    if (e instanceof IntentionalException) {
                        contextRunner.runOnContext(() -> {
                            throw (IntentionalException) e;
                        });
                    }
                    throw new RuntimeException(e);
                });
            }
        });
    }

    protected final AsyncAuthorizationGenerator<TestProfile> successfulPermissionAuthGenerator(final String permissionName) {
        return successfulAuthGenerator(profileToDecorate -> profileToDecorate.addPermission(permissionName));
    }

    protected final AsyncAuthorizationGenerator<TestProfile> successfulAuthGenerator(final Consumer<TestProfile> profileModifier) {
        return profile -> {
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
        return profile -> {
            LOG.info("Starting failing permission generator processing for profile");
            final CompletableFuture<Consumer<TestProfile>> future = new CompletableFuture<>();
            rule.vertx().setTimer(300, l -> {
                LOG.info("Completing failing profile decorator");
                future.completeExceptionally(new IntentionalException());
            });
            return future;
        };
    }


    protected AsyncBaseClient<TestCredentials, TestProfile> happyPathClient() {
        return new AsyncBaseClient<TestCredentials, TestProfile>(contextRunner) {
            @Override
            protected CompletableFuture<TestProfile> retrieveUserProfileFuture(TestCredentials credentials, WebContext context) {
                final CompletableFuture<TestProfile> future = new CompletableFuture<>();
                rule.vertx().setTimer(300, l -> {
                    future.complete(TestProfile.from(credentials));
                });
                return future;
            }

            @Override
            public HttpAction redirect(WebContext context) throws HttpAction {
                return null;
            }

            @Override
            public CompletableFuture<TestCredentials> getCredentials(WebContext context) {
                return null;
            }

            @Override
            public RedirectAction getLogoutAction(WebContext var1, TestProfile var2, String var3) {
                return null;
            }

            @Override
            protected void internalInit(WebContext webContext) {

            }
        };
    }

    private AsyncBaseClient<TestCredentials, TestProfile> failingRetrievalClient() {
        return new AsyncBaseClient<TestCredentials, TestProfile>(contextRunner) {
            @Override
            protected CompletableFuture<TestProfile> retrieveUserProfileFuture(TestCredentials credentials, WebContext context) {
                final CompletableFuture<TestProfile> future = new CompletableFuture<>();
                rule.vertx().setTimer(300, l -> {
                    future.completeExceptionally(new IntentionalException());
                });
                return future;
            }

            @Override
            public HttpAction redirect(WebContext context) throws HttpAction {
                return null;
            }

            @Override
            public CompletableFuture<TestCredentials> getCredentials(WebContext context) {
                return null;
            }

            @Override
            public RedirectAction getLogoutAction(WebContext var1, TestProfile var2, String var3) {
                return null;
            }

            @Override
            protected void internalInit(WebContext webContext) {

            }
        };
    }


}
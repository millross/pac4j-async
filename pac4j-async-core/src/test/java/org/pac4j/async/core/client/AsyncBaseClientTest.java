package org.pac4j.async.core.client;

import com.sun.tools.javac.util.List;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.redirect.RedirectAction;

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
    private static final String PERMISSION_ADMIN = "admin";
    private static final String PERMISSION_SYSTEM = "system";

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
                assertThat(p.getPermissions(), is(new HashSet(List.of(PERMISSION_ADMIN, PERMISSION_SYSTEM))));
                async.complete();
            });
        });

    }

    private final AsyncAuthorizationGenerator<TestProfile> successfulPermissionAuthGenerator(final String permissionName) {
        return profile -> {
            LOG.info("Starting processing for profile " + permissionName);
            final CompletableFuture<Consumer<TestProfile>> future = new CompletableFuture<>();
            final Consumer<TestProfile> profileDecorator = profileToDecorate -> {
                profile.addPermission(permissionName);
            };
            rule.vertx().setTimer(300, l -> {
                LOG.info("Completing profile decorator for " + permissionName);
                future.complete(profileDecorator);
            });
            return future;
        };
    }

    private AsyncBaseClient<TestCredentials, TestProfile> happyPathClient() {
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

}
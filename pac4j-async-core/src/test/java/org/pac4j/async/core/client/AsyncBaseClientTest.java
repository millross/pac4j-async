package org.pac4j.async.core.client;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.redirect.RedirectAction;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class AsyncBaseClientTest  extends VertxAsyncTestBase {

    private static final String HAPPY_PATH_NAME = "happyTestUser";
    private static final String HAPPY_PATH_PASSWORD = "happyPathPassword";

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
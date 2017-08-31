package org.pac4j.async.core.credentials.authenticator;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.IntentionalException;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.pac4j.async.core.util.TestsConstants.TEST_CREDENTIALS;

/**
 * Test for converting a blocking sync authenticator to an async authenticator
 */
public class AsyncAuthenticatorFromBlockingTest extends VertxAsyncTestBase {

    public AsyncWebContext webContext;
    public Authenticator<TestCredentials, WebContextBase<?>> authenticator = mock(Authenticator.class);
    public AsyncAuthenticator<TestCredentials> asyncAuthenticator = AsyncAuthenticator.fromBlocking(authenticator);

    @Before
    public void setupWebContext() {
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter).build();
    }

    @Test(timeout=1000)
    public void testFromNonBlockingExtractorSuccessfulEvaluation(final TestContext testContext) throws Exception {
        doAnswer(invocation -> {
            Thread.sleep(250);
            return null;
        }).when(authenticator).validate(eq(TEST_CREDENTIALS), eq(webContext));
        final Async async = testContext.async();
        final CompletableFuture<Void> authFuture = asyncAuthenticator.validate(TEST_CREDENTIALS, webContext);
        assertSuccessfulEvaluation(authFuture, res -> assertThat(res, is(nullValue())), async);
    }

    @Test(timeout=1000, expected=CredentialsException.class)
    public void testCredentialsExceptionBehaviour(final TestContext testContext) throws Exception {
        doAnswer(invocation -> {
            Thread.sleep(250);
            throw new CredentialsException("Intentional credentials exception");
        }).when(authenticator).validate(eq(TEST_CREDENTIALS), eq(webContext));
        final Async async = testContext.async();
        final CompletableFuture<Void> authFuture = asyncAuthenticator.validate(TEST_CREDENTIALS, webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }

    @Test(timeout=1000, expected=HttpAction.class)
    public void testHttpActionExceptionBehaviour(final TestContext testContext) throws Exception {
        doAnswer(invocation -> {
            Thread.sleep(250);
            throw HttpAction.status("Intentional http action", 200, webContext);
        }).when(authenticator).validate(eq(TEST_CREDENTIALS), eq(webContext));
        final Async async = testContext.async();
        final CompletableFuture<Void> authFuture = asyncAuthenticator.validate(TEST_CREDENTIALS, webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }

    @Test(timeout=1000, expected= IntentionalException.class)
    public void testUnexpectedExceptionBehaviour(final TestContext testContext) throws Exception {
        doThrow(new IntentionalException()).when(authenticator).validate(eq(TEST_CREDENTIALS), eq(webContext));
        final Async async = testContext.async();
        final CompletableFuture<Void> authFuture = asyncAuthenticator.validate(TEST_CREDENTIALS, webContext);
        assertSuccessfulEvaluation(authFuture, res   -> {}, async);
    }



}
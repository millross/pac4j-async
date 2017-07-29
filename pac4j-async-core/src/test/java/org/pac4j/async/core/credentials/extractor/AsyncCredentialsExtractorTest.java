package org.pac4j.async.core.credentials.extractor;

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
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pac4j.async.core.util.TestsConstants.PASSWORD;
import static org.pac4j.async.core.util.TestsConstants.USERNAME;

/**
 * Tests for AsyncCredentialsExtractor - specifically fromNonBlocking
 */
public class AsyncCredentialsExtractorTest extends VertxAsyncTestBase {

    public CredentialsExtractor<TestCredentials, WebContextBase<?>> extractor = mock(CredentialsExtractor.class);
    private static final TestCredentials CREDENTIALS = new TestCredentials(USERNAME, PASSWORD);
    public AsyncWebContext webContext;

    @Before
    public void setupWebContext() {
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
    }

    @Test(timeout=1000)
    public void testFromNonBlockingExtractorSuccessfulEvaluation(final TestContext testContext) throws Exception {
        when(extractor.extract(any(WebContextBase.class))).thenReturn(CREDENTIALS);
        final Async async = testContext.async();
        final CompletableFuture<TestCredentials> credsFuture = AsyncCredentialsExtractor.fromNonBlockingExtractor(extractor).extract(webContext);
        assertSuccessfulEvaluation(credsFuture, creds -> assertThat(creds, is(CREDENTIALS)), async);
    }

    @Test(timeout=1000, expected=CredentialsException.class)
    public void testCredentialsExceptionBehaviour(final TestContext testContext) throws Exception {
        when(extractor.extract(any(WebContextBase.class))).thenThrow(new CredentialsException("Intentional credentials exception"));
        final Async async = testContext.async();
        final CompletableFuture<TestCredentials> credsFuture = AsyncCredentialsExtractor.fromNonBlockingExtractor(extractor).extract(webContext);
        assertSuccessfulEvaluation(credsFuture, creds -> {}, async);
    }

    @Test(timeout=1000, expected=HttpAction.class)
    public void testHttpActionExceptionBehaviour(final TestContext testContext) throws Exception {
        final HttpAction action = HttpAction.status("Intentional http action", 200, webContext);
        when(extractor.extract(any(WebContextBase.class))).thenThrow(action);
        final Async async = testContext.async();
        final CompletableFuture<TestCredentials> credsFuture = AsyncCredentialsExtractor.fromNonBlockingExtractor(extractor).extract(null);
        assertSuccessfulEvaluation(credsFuture, creds -> {}, async);
    }

    @Test(timeout=1000, expected= IntentionalException.class)
    public void testUnexpectedExceptionBehaviour(final TestContext testContext) throws Exception {
        when(extractor.extract(any(WebContextBase.class))).thenThrow(new IntentionalException());
        final Async async = testContext.async();
        final CompletableFuture<TestCredentials> credsFuture = AsyncCredentialsExtractor.fromNonBlockingExtractor(extractor).extract(null);
        assertSuccessfulEvaluation(credsFuture, creds -> {}, async);
    }

}
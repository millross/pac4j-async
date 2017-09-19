package org.pac4j.async.core.context;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.VertxAsynchronousComputationAdapter;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.Cookie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class AsyncWebContextTest extends VertxAsyncTestBase {

    private static final String SESSION_ID = "SESSION_ID";
    private static final String NO_SUCH_KEY = "NO_SUCH_KEY";
    private static final String WRITE_KEY = "WRITE_KEY";
    public static final String WRITE_VALUE = "WRITE_VALUE";

    private AsyncSessionStore sessionStore = mock(AsyncSessionStore.class);

    private AsyncWebContext webContext;

    @Before
    public void setUp() {
        final Map<String, Object> session = new HashMap<>();

        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();
            final String key = (String) arguments[1];

            return delayedResult(() -> session.get(key));

        }).when(sessionStore).get(any(TestWebContext.class), anyString());

        doAnswer(invocation -> delayedResult(() -> SESSION_ID))
                .when(sessionStore).getOrCreateSessionId(any(TestWebContext.class));

        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();
            final String key = (String) arguments[1];
            final Object value = arguments[2];
            return delayedResult(() -> {
                session.put(key, value);
                return null;
            });
        }).when(sessionStore).set(any(TestWebContext.class), anyString(), anyObject());

        webContext = new TestWebContext(asynchronousComputationAdapter, sessionStore);
    }

    @Test(timeout = 1000)
    public void testGetSessionIdentifier(final TestContext testContext) {

        final Async async = testContext.async();

        webContext.getSessionStore().getOrCreateSessionId(webContext)
                .thenAccept(sid -> executionContext.runOnContext(() -> {
                    assertThat(sid, is(SESSION_ID));
                    async.complete();
                }));
    }

    @Test(timeout = 1000)
    public void testGetAndRetrieveSessionAttribute(final TestContext testContext) {
        final Async async = testContext.async();
        webContext.getSessionStore().set(webContext, WRITE_KEY, WRITE_VALUE)
                .thenCompose(v -> webContext.getSessionStore().get(webContext, WRITE_KEY))
                .thenAccept(val -> {
                   executionContext.runOnContext(() -> {
                       assertThat(val, is(WRITE_VALUE));
                       async.complete();
                   });
                });
    }

    @Test(timeout = 1000)
    public void testGetNonexistentSessionAttributeReturnsNull(final TestContext testContext) {
        final Async async = testContext.async();
        webContext.getSessionStore().get(webContext, NO_SUCH_KEY)
                .thenAccept(val -> executionContext.runOnContext(() -> {
                    assertThat(val, is(nullValue()));
                    async.complete();
                }));
    }

    private static class TestWebContext implements AsyncWebContext {

        private final AsyncSessionStore sessionStore;
        private final VertxAsynchronousComputationAdapter asyncComputationAdapter;

        private TestWebContext(
                final VertxAsynchronousComputationAdapter asyncComputationAdapter,
                final AsyncSessionStore sessionStore) {
            this.asyncComputationAdapter = asyncComputationAdapter;
            this.sessionStore = sessionStore;
        }

        @Override
        public AsyncSessionStore getSessionStore() {
            return sessionStore;
        }

        @Override
        public AsynchronousComputationAdapter getAsyncComputationAdapter() {
            return asyncComputationAdapter;
        }

        @Override
        public String getRequestParameter(String name) {
            return null;
        }

        @Override
        public Map<String, String[]> getRequestParameters() {
            return null;
        }

        @Override
        public Object getRequestAttribute(String name) {
            return null;
        }

        @Override
        public void setRequestAttribute(String name, Object value) {

        }

        @Override
        public String getRequestHeader(String name) {
            return null;
        }

        @Override
        public String getRequestMethod() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public void writeResponseContent(String content) {

        }

        @Override
        public void setResponseStatus(int code) {

        }

        @Override
        public void setResponseHeader(String name, String value) {

        }

        @Override
        public void setResponseContentType(String content) {

        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getFullRequestURL() {
            return null;
        }

        @Override
        public Collection<Cookie> getRequestCookies() {
            return null;
        }

        @Override
        public void addResponseCookie(Cookie cookie) {

        }

        @Override
        public String getPath() {
            return null;
        }


    }
}
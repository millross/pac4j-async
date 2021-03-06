package org.pac4j.async.core;

import io.vertx.core.Vertx;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.HttpConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.pac4j.core.context.HttpConstants.HTTP_METHOD.GET;

/**
 *
 */
public class MockAsyncWebContextBuilder {

    public static final String DEFAULT_FULL_REQUEST_URL = "http://localhost:80";

    private Consumer<AsyncWebContext> constructor;
    private HttpConstants.HTTP_METHOD httpMethod = GET;
    private final Map<String, Object> dummySession = new HashMap<>();
    private final Map<String, Object> requestAttributes = new HashMap<>();

    public static MockAsyncWebContextBuilder from(final Vertx vertx ,
                                                  final AsynchronousComputationAdapter asyncComputationAdapter) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(asyncComputationAdapter);
        return new MockAsyncWebContextBuilder(vertx, asyncComputationAdapter);
    }

    private MockAsyncWebContextBuilder(final Vertx vertx,
            final AsynchronousComputationAdapter asynchronousComputationAdapter) {

        constructor = webContext -> {
            // Set up execution context
            when(webContext.getExecutionContext()).thenReturn(asynchronousComputationAdapter.getExecutionContext());
            when(webContext.getAsyncComputationAdapter()).thenReturn(asynchronousComputationAdapter);

            final AsyncSessionStore sessionStore = mockSessionStore(vertx);

            when(webContext.getSessionStore()).thenReturn(sessionStore);

            // Set up request attributes
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                Object value = args[1];
                requestAttributes.put(key, value);
                return null;
            }).when(webContext).setRequestAttribute(anyString(), anyObject());

            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                return requestAttributes.get(key);

            }).when(webContext).getRequestAttribute(anyString());

            when(webContext.getFullRequestURL()).thenReturn(DEFAULT_FULL_REQUEST_URL);
            when(webContext.getRequestMethod()).thenReturn(httpMethod.name());
        };

    }

    public MockAsyncWebContextBuilder withScheme(final String scheme) {
        constructor = constructor.andThen(webContext -> when(webContext.getScheme()).thenReturn(scheme));
        return this;
    }

    public MockAsyncWebContextBuilder withRecordedResponseHeaders(final Map<String, String> writtenHeaders) {
        Objects.requireNonNull(writtenHeaders);
        constructor = constructor.andThen(webContext -> {
            doAnswer(invocation -> {
                final String headerName = invocation.getArgumentAt(0, String.class);
                final String headerValue = invocation.getArgumentAt(1, String.class);
                writtenHeaders.put(headerName, headerValue);
                return null;
            }).when(webContext).setResponseHeader(anyString(), anyString());

        });
        return this;
    }

    public MockAsyncWebContextBuilder withRecordedResponseCookies(final Map<String, Cookie> writtenCookies) {
        constructor = constructor.andThen(webContext -> {
            doAnswer(invocation ->  {
                final Cookie cookie = invocation.getArgumentAt(0, Cookie.class);
                writtenCookies.put(cookie.getName(), cookie);
                return null;
            }).when(webContext).addResponseCookie(any(Cookie.class));

        });
        return this;
    }

    public MockAsyncWebContextBuilder withStatusRecording(final AtomicInteger status) {
        status.set(-1);
        constructor = constructor.andThen(webContext -> {
            doAnswer(invocation -> {
                if (status.get() != -1) {
                    throw new RuntimeException("Status has already been set");
                }
                final int newStatus = invocation.getArgumentAt(0, Integer.class).intValue();
                status.set(newStatus);
                return null;
            }).when(webContext).setResponseStatus(anyInt());
        });
        return this;
    }

    public MockAsyncWebContextBuilder withResponseContentRecording(final StringBuffer stringBuffer) {
        constructor = constructor.andThen(webContext -> {
            doAnswer(invocation -> {
                final String contentToAppend = invocation.getArgumentAt(0, String.class);
                stringBuffer.append(contentToAppend);
                return null;
            }).when(webContext).writeResponseContent(anyString());
        });
        return this;
    }

    public MockAsyncWebContextBuilder withRequestMethod(final HttpConstants.HTTP_METHOD method) {
        this.httpMethod = method;
        return this;
    }

    public AsyncWebContext build() {
        final AsyncWebContext webContext = mock(AsyncWebContext.class);
        constructor.accept(webContext);

        return webContext;
    }

    private final AsyncSessionStore mockSessionStore(final Vertx vertx) {

        final AsyncSessionStore sessionStore = mock(AsyncSessionStore.class);

        // Set up session attributes
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String key = (String) args[1];
            Object value = args[2];
            final CompletableFuture<Void> future = new CompletableFuture<>();
            vertx.setTimer(300, v -> {
                dummySession.put(key, value);
                future.complete(null);
            });
            return future;
        }).when(sessionStore).set(anyObject(), anyString(), anyObject());

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String key = (String) args[1];
            final CompletableFuture<Object> future = new CompletableFuture<>();
            vertx.setTimer(300, v -> future.complete(dummySession.get(key)));
            return future;
        }).when(sessionStore).get(any(AsyncWebContext.class), anyString());

        return sessionStore;

    }



}

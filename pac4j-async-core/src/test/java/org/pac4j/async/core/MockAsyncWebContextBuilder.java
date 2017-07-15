package org.pac4j.async.core;

import io.vertx.core.Vertx;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.HttpConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
                                                  final AsyncPac4jExecutionContext executionContext) {
        return new MockAsyncWebContextBuilder(vertx, executionContext);
    }

    private MockAsyncWebContextBuilder(final Vertx vertx,
            final AsyncPac4jExecutionContext executionContext) {

        constructor = webContext -> {
            // Set up execution context
            when(webContext.getExecutionContext()).thenReturn(executionContext);

            // Set up session attributes
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                Object value = args[1];
                final CompletableFuture<Void> future = new CompletableFuture<>();
                vertx.setTimer(300, v -> {
                    dummySession.put(key, value);
                    future.complete(null);
                });
                return future;
            }).when(webContext).setSessionAttribute(anyString(), anyObject());

            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                final CompletableFuture<Object> future = new CompletableFuture<>();
                vertx.setTimer(300, v -> future.complete(dummySession.get(key)));
                return future;
            }).when(webContext).getSessionAttribute(anyString());

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

    public MockAsyncWebContextBuilder withRequestMethod(final HttpConstants.HTTP_METHOD method) {
        this.httpMethod = method;
        return this;
    }

    public AsyncWebContext build() {
        final AsyncWebContext webContext = mock(AsyncWebContext.class);
        constructor.accept(webContext);

        return webContext;
    }

}

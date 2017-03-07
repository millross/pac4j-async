package org.pac4j.async.core.authorization.authorizer.csrf;

import com.aol.cyclops.closures.mutable.Mutable;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.pac4j.async.core.context.AsyncWebContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.pac4j.core.context.Pac4jConstants.CSRF_TOKEN;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultAsyncCsrfTokenGenerator.class)
public class DefaultAsyncCsrfTokenGeneratorTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();
    private static final UUID SESSION_UUID = UUID.randomUUID();

    private Vertx vertx;
    private AsyncWebContext<String> webContext;
    private final Mutable<String> uidFromSession = Mutable.of(null);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        webContext = mock(AsyncWebContext.class);
        uidFromSession.set(null);
        when(webContext.getSessionAttribute(CSRF_TOKEN))
                .thenReturn(delayedCompletion(() -> uidFromSession.get()));
        doAnswer((InvocationOnMock invocation) -> {
            final Object value = invocation.getArguments()[1 ];
            return delayedCompletion(() -> {
                uidFromSession.set((String)value);
                return null;
            });
        }).when(webContext).setSessionAttribute(eq(CSRF_TOKEN), anyObject());
    }

    @After
    public void cleanup() {
        vertx.close();
    }

    @Test
    public void testCorrectBehaviourWhenTokenInSession() throws Exception {

        uidFromSession.set(SESSION_UUID.toString());
        PowerMockito.mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(RANDOM_UUID);

        final CompletableFuture<String> future = new CompletableFuture<>();
        final DefaultAsyncCsrfTokenGenerator generator = new DefaultAsyncCsrfTokenGenerator();
        vertx.runOnContext(v -> {
            final CompletableFuture<String> csrfToken = generator.get(webContext);
            csrfToken.thenAccept(s -> future.complete(s));
        });
        final String retrievedCsrfToken = future.get();
        assertThat(retrievedCsrfToken, is(SESSION_UUID.toString()));
    }

    @Test
    public void testCorrectBehaviourWhenNoTokenInSession() throws Exception {

        PowerMockito.mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(RANDOM_UUID);

        final CompletableFuture<String> future = new CompletableFuture<>();
        final DefaultAsyncCsrfTokenGenerator generator = new DefaultAsyncCsrfTokenGenerator();
        vertx.runOnContext(v -> {
            final CompletableFuture<String> csrfToken = generator.get(webContext);
            csrfToken.thenAccept(s -> future.complete(s));
        });
        final String retrievedCsrfToken = future.get();
        assertThat(retrievedCsrfToken, is(RANDOM_UUID.toString()));
    }

    private <T> CompletableFuture<T> delayedCompletion(final T value) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        vertx.setTimer(250, v -> future.complete(value));
        return future;
    }

    private <T> CompletableFuture<T> delayedCompletion(final Supplier<T> valueSupplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        vertx.setTimer(250, v -> {
            final T t = valueSupplier.get();
            future.complete(t);
        });
        return future;
    }

}
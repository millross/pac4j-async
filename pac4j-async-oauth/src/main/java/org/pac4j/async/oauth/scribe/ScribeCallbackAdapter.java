package org.pac4j.async.oauth.scribe;

import com.github.scribejava.core.model.OAuthAsyncRequestCallback;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter to take a CompletableFuture and create a Scribe callback which will complete the future either successfully or
 * exceptionally when it is called.
 */
public class ScribeCallbackAdapter {

    public static <T> OAuthAsyncRequestCallback<T> toScribeOAuthRequestCallback(final CompletableFuture<T> future) {
        return new OAuthAsyncRequestCallback<T>() {
            @Override
            public void onCompleted(T response) {
                future.complete(response);
            }

            @Override
            public void onThrowable(Throwable t) {
                future.completeExceptionally(t);
            }
        };
    }

}

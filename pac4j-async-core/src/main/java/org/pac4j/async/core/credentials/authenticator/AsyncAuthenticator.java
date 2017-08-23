package org.pac4j.async.core.credentials.authenticator;

import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;

import java.util.concurrent.CompletableFuture;

import static com.aol.cyclops.invokedynamic.ExceptionSoftener.softenRunnable;

/**
 * Asynchronous credentials authenticator to be used by async clients
 */
public interface AsyncAuthenticator<C extends Credentials> {
    /**
     * Validate the credentials. It is assumed that this could be an asynchronous process so will yield a
     * CompletableFuture void. A successful completion implies acceptable credentials.
     *
     * Exceptional completion with an HttpAction means a specific Http action is required
     * Exceptional completion with credentials exception means credentials are invalid
     *
     */
    CompletableFuture<Void> validate(C credentials, AsyncWebContext context);

    static <T extends Credentials> AsyncAuthenticator<T> fromNonBlocking(final Authenticator<T, WebContextBase<?>> syncAuthenticator) {
        return (credentials, context) -> AsynchronousComputationAdapter.fromNonBlocking(softenRunnable(() -> syncAuthenticator.validate(credentials, context)));
    }
}

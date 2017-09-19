package org.pac4j.async.core.credentials.authenticator;

import org.pac4j.async.core.AsynchronousComputationAdapter;
import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContext;
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

    static <T extends Credentials> AsyncAuthenticator<T> fromNonBlocking(final Authenticator<T, WebContext<?>> syncAuthenticator) {
        return (credentials, context) -> AsynchronousComputationAdapter.fromNonBlocking(softenRunnable(() -> syncAuthenticator.validate(credentials, context)));
    }

    static <C extends Credentials> AsyncAuthenticator<C> fromBlocking(Authenticator<C, WebContext<?>> blockingAuth) {
        return (credentials, context) -> context.getAsyncComputationAdapter()
                .fromBlocking(ExceptionSoftener.softenRunnable(() -> blockingAuth.validate(credentials, context)));
    }
}

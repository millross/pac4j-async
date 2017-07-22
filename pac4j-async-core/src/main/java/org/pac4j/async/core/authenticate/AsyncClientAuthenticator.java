package org.pac4j.async.core.authenticate;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Class which, for a single client being asked to perform an authentication, actually carries out that authentication,
 * returning an Optional<P extends profile> for the result
 *
 * Returns a CompletableFuture<Optional<P>> which will complete with Some(profile) if profile was present, otherwise
 * empty optional.
 *
 * This will not engage with saving of the profile found, it's a convenient coarser-grained wrapper for the two key
 * operations of the AsyncClient class (and arguably should be exposed by client classes rather than in its own)
 *
 */
public class AsyncClientAuthenticator<U extends CommonProfile, WC extends AsyncWebContext> {

    public <P extends U, C extends Credentials> CompletableFuture<Optional<P>> authenticateFor(final AsyncClient<C, P> client,
                                                                                               final WC webContext) {
        return client.getCredentials(webContext)
                .thenCompose(c -> client.getUserProfileFuture(c, webContext));
    }

}

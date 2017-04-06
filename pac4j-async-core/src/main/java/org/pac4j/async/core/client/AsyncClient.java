package org.pac4j.async.core.client;

import org.pac4j.async.core.Named;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing asynchronous client capability. The key in most cases is that each method in the AsyncClient
 * should accept a CompletableFuture to be completed with the result of whatever operation we are attempting to
 * perform asynchronously.
 *
 * getMame() can be assumed not to involve any i/o so we leave this synchronous.
 * Initially we will also assume that this is also the case for redirect (should this be a general client method?)
 *
 */
public interface AsyncClient<C extends Credentials, U extends CommonProfile> extends Named, ConfigurableByClientsObject<AsyncClient<C, U>,  AsyncAuthorizationGenerator<U>> {

    /**
     * <p>Redirect to the authentication provider for an indirect client.</p>
     *
     * @param context the current web context
     * @return the performed redirection
     * @throws HttpAction whether an additional HTTP action is required
     */
    HttpAction redirect(AsyncWebContext context) throws HttpAction;

    /**
     * <p>Get the credentials from the web context. If no validation was made remotely (direct client), credentials must be validated at this step.</p>
     * <p>In some cases, a {@link HttpAction} may be thrown instead.</p> This version of the method assumes that credentials
     * retrieval can require i/o, which should be performed asynchronously. Therefore, return is wrapped in a future so that once
     * the credentials are retrieved, it can be completed and the client code can then proceed. If no i/o is required, then
     * the future can be completed immediately on-thread. We expect subsequent behaviour to be performed using the composition
     * properties of CompletableFutures.
     *
     * Where an HttpAction or TechnicalException would normally be thrown by sync pac4j,
     * now this should be passed to CompletableFuture::completeExceptionally
     *
     * @param context the current web context
     * @return a CompletableFuture wrapping the asynchronous processing
     */
    CompletableFuture<C> getCredentials(AsyncWebContext context);

    /**
     * Get the user profile based on the provided credentials. Again this may involve i/o so we treat it as an
     * async operation. If no i/o is involved then the future can be completed immediately. If i/o is involved
     * then the i/o should occur off the calling thread and the future should be completed with the value when it
     * is retrieved.
     *
     * Where an HttpAction or TechnicalException would normally be thrown by sync pac4j,
     * now this should be passed to CompletableFuture::completeExceptionally
     *
     * @param credentials credentials
     * @param context web context
     *
     * @return future wrapping the value which the computation will generate. The computuation should complete
     * this future when the value has been generated.
     */
    CompletableFuture<Optional<U>> getUserProfileFuture(C credentials, AsyncWebContext context);

    RedirectAction getLogoutAction(AsyncWebContext var1, U var2, String var3);

    /**
     * Return true if this client is indirect - i.e. assumes that its profile will be stored in session and that a
     * redirect to an auth provider will be required if no profile is found.
     * @return true if indirect, false if direct
     */
    boolean isIndirect();
}

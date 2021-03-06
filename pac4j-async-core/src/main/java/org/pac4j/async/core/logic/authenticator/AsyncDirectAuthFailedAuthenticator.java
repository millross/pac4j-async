package org.pac4j.async.core.logic.authenticator;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.logic.AsyncIndirectAuthenticationFlow;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.pac4j.core.util.CommonHelper.isNotEmpty;

/**
 * Async authenticator to handle the situation when direct client authentication has yielded no profiles (i.e. when
 * direct client authentication has failed).
 *
 * This authenticator should trigger indirect authentication if an indirect client is availble to trigger authentication
 * otherwise it should identify an authentication failure
 */
public class AsyncDirectAuthFailedAuthenticator<C extends AsyncWebContext> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncDirectAuthFailedAuthenticator.class);

    private final AsyncIndirectAuthenticationFlow<C> indirectAuthenticationInitiator = new AsyncIndirectAuthenticationFlow<>();

    public final  <U extends CommonProfile> CompletableFuture<HttpAction> authenticate(final C context, final List<AsyncClient<? extends Credentials, U>> currentClients) {

        if (startAuthentication(context, currentClients)) {
            logger.debug("Starting authentication");
            return indirectAuthenticationInitiator.initiateIndirectFlow(context, currentClients);
        } else {
            logger.debug("unauthorized");
            return CompletableFuture.completedFuture(unauthorized(context, currentClients));
        }
    }

    /**
     * Return whether we must start a login process if the first client is an indirect one.
     *
     * @param context the web context
     * @param currentClients the current clients
     * @return whether we must start a login process
     */
    protected <U extends CommonProfile> boolean startAuthentication(final C context,
                                          final List<AsyncClient<? extends Credentials, U>> currentClients) {
        return isNotEmpty(currentClients) && currentClients.get(0).isIndirect();
    }

    /**
     * Return an unauthorized error.
     *
     * @param context the web context
     * @param currentClients the current clients
     * @return an unauthorized error
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected <U extends CommonProfile> HttpAction unauthorized(final C context, final List<AsyncClient<? extends Credentials, U>> currentClients) {
        return HttpAction.unauthorized("unauthorized", context, null);
    }

}

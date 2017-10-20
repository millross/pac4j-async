package org.pac4j.async.core.logic;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Class to wrap the initiation of an indirect authentication flow, following failure of authentication via direct
 * clients. This initiation includes both saving of the redirect url in the session and then triggering the redirect
 *
 */
public class AsyncIndirectAuthenticationFlow<C extends AsyncWebContext> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncIndirectAuthenticationFlow.class);

    public final <U extends CommonProfile> CompletableFuture<HttpAction> initiateIndirectFlow(final C context,
                                                                    final List<? extends AsyncClient<? extends Credentials, U>> currentClients) {

        logger.debug("Starting indirect flow authentication");
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);

        // Save redirect url then trigger redirect
        return context.getSessionStore().set(context, Pac4jConstants.REQUESTED_URL, requestedUrl)
                .thenCompose(v -> currentClients.stream().filter(AsyncClient::isIndirect)
                    // No need to cast, redirect override takes care of this.
                    .findFirst()
                    .map(ExceptionSoftener.softenFunction(c -> c.redirect(context)))
                    .orElseThrow(() -> new TechnicalException("No indirect client available for redirect")));
    }

    public final CompletableFuture<HttpAction> redirectToOriginallyRequestedUrl(final C context, final String defaultUrl) {
        return context.getSessionStore().<String>get(context, Pac4jConstants.REQUESTED_URL)
                .thenApply(Optional::ofNullable)
                .thenApply(o -> o.orElse(defaultUrl))
                .thenCompose(v -> context.getSessionStore().set(context, Pac4jConstants.REQUESTED_URL, null)
                        .thenApply(voidResult -> {
                            logger.debug("redirectUrl: {}", v);
                            return HttpAction.redirect("redirect", context, v);
                        }));
    }

}

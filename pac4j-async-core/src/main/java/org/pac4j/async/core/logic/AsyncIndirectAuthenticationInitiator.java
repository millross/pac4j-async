package org.pac4j.async.core.logic;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class to wrap the initiation of an indirect authentication flow, following failure of authentication via direct
 * clients. This initiation includes both saving of the redirect url in the session and then triggering the redirect
 *
 */
public class AsyncIndirectAuthenticationInitiator<C extends AsyncWebContext> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncIndirectAuthenticationInitiator.class);

    public final CompletableFuture<HttpAction> initiateIndirectFlow(final C context, final List<AsyncClient> currentClients) {

        logger.debug("Starting indirect flow authentication");
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);

        // Save redirect url then trigger redirect
        return context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl)
                .thenApply(v -> currentClients.stream().filter(AsyncClient::isIndirect)
                    // No need to cast, redirect override takes care of this.
                    .findFirst()
                    .map(ExceptionSoftener.softenFunction(c -> c.redirect(context)))
                    .orElseThrow(() -> new TechnicalException("No indirect client available for redirect")));
    }

}

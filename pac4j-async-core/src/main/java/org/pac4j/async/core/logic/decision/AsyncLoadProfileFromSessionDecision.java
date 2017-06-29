package org.pac4j.async.core.logic.decision;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;

import java.util.List;

import static org.pac4j.core.util.CommonHelper.isEmpty;

/**
 * Decision class for determining (for logics) whether or not to load profiles from the session
 */
public class AsyncLoadProfileFromSessionDecision<C extends AsyncWebContext> {
    /**
     * Load the profiles from the web context if no clients are defined or if the first client is an indirect one or is
     * an anonymouc client
     *
     * @param context the web context
     * @param currentClients the current clients
     * @return whether the profiles must be loaded from the web session
     */
    public boolean make(final C context, final List<AsyncClient> currentClients) {
        return isEmpty(currentClients) || currentClients.get(0).isIndirect() || currentClients.get(0).isAnonymous();
    }
}

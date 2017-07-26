package org.pac4j.async.core.logic.decision;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class AsyncSaveProfileToSessionDecision<U extends CommonProfile, C extends AsyncWebContext> {

    private final boolean saveToSession;

    public AsyncSaveProfileToSessionDecision(boolean useSession) {
        saveToSession = useSession;
    }

    public boolean make(final C context, final List<AsyncClient> currentClients,
                        final AsyncClient directClient, final U profile) {
        return saveToSession;
    }

}

package org.pac4j.core.client.finder;

import org.pac4j.async.core.Named;
import org.pac4j.async.core.client.ConfigurableByClientsObject;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;

import java.util.List;

/**
 * Generic client finder interface which can be used for either async or sync clients, based on a generic type, the
 * intention is that this would apply to either AsyncClient or (Sync) Client but the logic remains the same
 */

public interface ClientFinder<C extends Named & ConfigurableByClientsObject> {
    List<C> find(Clients<C, ?> clients, WebContext<?> context, String clientNames);
}

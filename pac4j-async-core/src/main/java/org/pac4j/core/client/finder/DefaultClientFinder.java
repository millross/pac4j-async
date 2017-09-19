package org.pac4j.core.client.finder;

import org.pac4j.async.core.Named;
import org.pac4j.async.core.client.ConfigurableByClientsObject;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class DefaultClientFinder<C extends Named & ConfigurableByClientsObject> implements ClientFinder<C> {
    @Override
    public List<C> find(Clients<C, ?> clients, WebContext<?> context, String clientNames) {
        final List<C> result = new ArrayList<>();

        if (CommonHelper.isNotBlank(clientNames)) {
            final List<String> names = Arrays.asList(clientNames.split(Pac4jConstants.ELEMENT_SEPRATOR));
            // if a client_name parameter is provided on the request, get the client and check if it is allowed
            final String clientNameOnRequest = context.getRequestParameter(clients.getClientNameParameter());
            if (clientNameOnRequest != null) {
                // from the request
                final C client = clients.findClient(context);
                final String nameFound = client.getName();
                // if allowed -> return it
                boolean found = false;
                for (final String name : names) {
                    if (CommonHelper.areEqualsIgnoreCaseAndTrim(name, nameFound)) {
                        result.add(client);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new TechnicalException("Client not allowed: " + nameFound);
                }
            } else {
                // no client provided, return all
                for (final String name : names) {
                    // from its name
                    final C client = clients.findClient(name);
                    result.add(client);
                }
            }
        }
        return result;
    }
}

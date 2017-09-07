package org.pac4j.async.core.logout;

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.client.Clients;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Strategy interface to represent central logout
 */
public interface AsyncCentralLogoutStrategy {
    <U extends CommonProfile> Optional<Supplier<HttpAction>> getCentralLogoutAction(Clients<AsyncClient<? extends Credentials, ? extends U>, AsyncAuthorizationGenerator<U>> clients, List<? extends U> profiles, String redirectUrl, AsyncWebContext webContext);
}

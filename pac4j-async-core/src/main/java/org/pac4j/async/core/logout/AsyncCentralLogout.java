package org.pac4j.async.core.logout;

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enumeration for default central logout behaviours (these can potentially be overloaded if desired)
 */
public enum AsyncCentralLogout implements AsyncCentralLogoutStrategy {

    /**
     * Do not trigger a central logout behaviour
     */
    NO_CENTRAL_LOGOUT {
        @Override
        public <U extends CommonProfile> Optional<Supplier<HttpAction>> getCentralLogoutAction(Clients<AsyncClient<? extends Credentials, ? extends U>, AsyncAuthorizationGenerator<U>> clients, List<? extends U> profiles, String redirectUrl, AsyncWebContext webContext) {
            LOG.debug("No central logout is configured");
            return Optional.empty();
        }
    },
    /**
     * Trigger default central logout behaviour
     */
    CENTRAL_LOGOUT {
        @Override
        public <U extends CommonProfile> Optional<Supplier<HttpAction>> getCentralLogoutAction(Clients<AsyncClient<? extends Credentials, ? extends U>, AsyncAuthorizationGenerator<U>> clients, List<? extends U> profiles, String redirectUrl, final AsyncWebContext webContext) {
            LOG.debug("Performing central logout");

            return profiles.stream()
                    .peek(profile -> LOG.debug("Profile: {}", profile))
                    .map(profile -> getRedirectAction(profile, clients, redirectUrl, webContext))
                    .map(redirectActionOption -> redirectActionOption.map((Function<RedirectAction, Supplier<HttpAction>>) redirectAction -> () -> redirectAction.perform(webContext)))
                    .filter(Optional::isPresent)
                    .findFirst()
                    .map(o -> o.get());

        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(AsyncCentralLogout.class);

    private static <U extends CommonProfile, C extends Credentials, T extends U> Optional<RedirectAction> getRedirectAction(final U profile, final Clients<AsyncClient<? extends Credentials, ? extends U>, AsyncAuthorizationGenerator<U>> clients, final String redirectUrl, final AsyncWebContext webContext) {
        final Optional<AsyncClient<C, T>> clientOption = Optional.ofNullable(profile.getClientName())
                .flatMap(clientName -> Optional.ofNullable(clients.findClient(clientName)));
        final Optional<String> targetUrl = clientOption.flatMap(client -> getTargetUrl(redirectUrl));
        return clientOption.map(client -> getLogoutAction(client, webContext, (T) profile, targetUrl.orElse(null)));
    }

    private static <T extends CommonProfile, C extends Credentials> RedirectAction getLogoutAction(final AsyncClient<C, T> client,
                                                                                                   final AsyncWebContext webContext,
                                                                                                   final T profile,
                                                                                                   final String targetUrl) {
        return client.getLogoutAction(webContext, profile, targetUrl);
    }

    private static <T extends CommonProfile, C extends Credentials>  Optional<String> getTargetUrl(final String redirectUrl) {
        return Optional.ofNullable(redirectUrl).flatMap(url -> {
            if (url.startsWith(HttpConstants.SCHEME_HTTP) || url.startsWith(HttpConstants.SCHEME_HTTPS)) {
                return Optional.of(url);
            } else {
                return Optional.empty();
            }
        });
    }

}

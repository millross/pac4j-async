package org.pac4j.core.config;

import org.pac4j.async.core.Named;
import org.pac4j.async.core.client.ConfigurableByClientsObject;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public class Config<T extends Named & ConfigurableByClientsObject, C extends WebContextBase<?>, A, M, SL, CL, LL, S, PM, U extends CommonProfile>  {

    protected Clients<T, AuthorizationGenerator<C, U>> clients;

    protected Map<String, A> authorizers = new HashMap<>();

    protected Map<String, M> matchers = new HashMap<>();

    protected S sessionStore;

    protected HttpActionAdapter httpActionAdapter;

    protected SL securityLogic;

    protected CL callbackLogic;

    protected LL logoutLogic;

    protected Function<C, PM> profileManagerFactory;

    public Config() {}

    public Config(final T client) {
        this.clients = new Clients(client);
    }

    public Config(final Clients clients) {
        this.clients = clients;
    }

    public Config(final List<T> clients) {
        this.clients = new Clients(clients);
    }

    public Config(final T... clients) {
        this.clients = new Clients(clients);
    }

    public Config(final String callbackUrl, final T client) {
        this.clients = new Clients(callbackUrl, client);
    }

    public Config(final String callbackUrl, final T... clients) {
        this.clients = new Clients(callbackUrl, clients);
    }

    public Config(final String callbackUrl, final List<T> clients) {
        this.clients = new Clients(callbackUrl, clients);
    }

    public Config(final Map<String, A> authorizers) {
        setAuthorizers(authorizers);
    }

    public Config(final Clients clients, final Map<String, A> authorizers) {
        this.clients = clients;
        setAuthorizers(authorizers);
    }

    public Config(final T client, final Map<String, A> authorizers) {
        this.clients = new Clients(client);
        setAuthorizers(authorizers);
    }

    public Config(final Map<String, A> authorizers, final T... clients) {
        this.clients = new Clients(clients);
        setAuthorizers(authorizers);
    }

    public Config(final String callbackUrl, final Map<String, A> authorizers, final T... clients) {
        this.clients = new Clients(callbackUrl, clients);
        setAuthorizers(authorizers);
    }

    public Config(final String callbackUrl, final T client, final Map<String, A> authorizers) {
        this.clients = new Clients(callbackUrl, client);
        setAuthorizers(authorizers);
    }

    public Clients<T, AuthorizationGenerator<C, U>> getClients() {
        return clients;
    }

    public void setClients(final Clients clients) {
        this.clients = clients;
    }

    public Map<String, A> getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizer(final A authorizer) {
        CommonHelper.assertNotNull("authorizer", authorizer);
        this.authorizers.put(authorizer.getClass().getSimpleName(), authorizer);
    }

    public void setAuthorizers(final Map<String, A> authorizers) {
        CommonHelper.assertNotNull("authorizers", authorizers);
        this.authorizers = authorizers;
    }

    public void addAuthorizer(final String name, final A authorizer) {
        authorizers.put(name, authorizer);
    }

    public Map<String, M> getMatchers() {
        return matchers;
    }

    public void setMatcher(final M matcher) {
        CommonHelper.assertNotNull("matcher", matcher);
        this.matchers.put(matcher.getClass().getSimpleName(), matcher);
    }

    public void setMatchers(final Map<String, M> matchers) {
        CommonHelper.assertNotNull("matchers", matchers);
        this.matchers = matchers;
    }

    public void addMatcher(final String name, final M matcher) {
        matchers.put(name, matcher);
    }

    public S getSessionStore() {
        return sessionStore;
    }

    public void setSessionStore(final S sessionStore) {
        this.sessionStore = sessionStore;
    }

    public HttpActionAdapter getHttpActionAdapter() {
        return httpActionAdapter;
    }

    public void setHttpActionAdapter(final HttpActionAdapter httpActionAdapter) {
        this.httpActionAdapter = httpActionAdapter;
    }

    public SL getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SL securityLogic) {
        this.securityLogic = securityLogic;
    }

    public CL getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(final CL callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public LL getLogoutLogic() {
        return logoutLogic;
    }

    public void setLogoutLogic(final LL logoutLogic) {
        this.logoutLogic = logoutLogic;
    }

    public Function<C, PM> getProfileManagerFactory() {
        return profileManagerFactory;
    }

    public void setProfileManagerFactory(final Function<C, PM> profileManagerFactory) {
        this.profileManagerFactory = profileManagerFactory;
    }

}

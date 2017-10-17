package org.pac4j.async.oauth.credentials.authenticator;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.credentials.authenticator.AsyncAuthenticator;
import org.pac4j.async.oauth.config.OAuthConfiguration;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.InitializableWebObject;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of OAuthAuthenticator
 */
public abstract class AsyncOAuthAuthenticator<C extends OAuthCredentials, O extends OAuthConfiguration> extends InitializableWebObject<AsyncWebContext> implements AsyncAuthenticator<C> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncOAuthAuthenticator.class);

    protected final O configuration;

    protected AsyncOAuthAuthenticator(final O configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void internalInit(AsyncWebContext context) {
        CommonHelper.assertNotNull("configuration", this.configuration);
        configuration.init(context);
    }

    @Override
    public CompletableFuture<Void> validate(C credentials, AsyncWebContext context) {
        init(context);
        return retrieveAccessToken(credentials);
    }

    /**
     * Retrieve the access token from OAuth credentials.
     *
     * @param credentials credentials
     * Return a CompletableFuture<Void> which will complete successfully on successful validation
     * or exceptionally with a HttpAction if an additional HTTP action is required
     * or exceptionally with an OAuthCredentialsException if the credentials are invalid
     */
    protected abstract CompletableFuture<Void> retrieveAccessToken(OAuthCredentials credentials);

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "configuration", this.configuration);
    }

}

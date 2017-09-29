package org.pac4j.async.oauth.credentials.authenticator;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.pac4j.async.oauth.scribe.ScribeCallbackAdapter;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.credentials.OAuthCredentials;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AsyncOAuth20Authenticator extends AsyncOAuthAuthenticator<OAuth20Credentials, OAuth20Configuration>{

    public AsyncOAuth20Authenticator(final OAuth20Configuration configuration) {
        super(configuration);
    }

    @Override
    protected CompletableFuture<Void> retrieveAccessToken(OAuthCredentials credentials) {
        final OAuth20Credentials oAuth20Credentials = (OAuth20Credentials) credentials;
        // no request token saved in context and no token (OAuth v2.0)
        final String code = oAuth20Credentials.getCode();
        logger.debug("code: {}", code);
        final CompletableFuture<OAuth2AccessToken> accessTokenFuture = new CompletableFuture<>();

        this.configuration.getService().getAccessTokenAsync(code, ScribeCallbackAdapter.toScribeOAuthRequestCallback(accessTokenFuture));

        return accessTokenFuture.thenAccept(accessToken -> {
            logger.debug("accessToken: {}", accessToken);
            oAuth20Credentials.setAccessToken(accessToken);
        });
    }
}

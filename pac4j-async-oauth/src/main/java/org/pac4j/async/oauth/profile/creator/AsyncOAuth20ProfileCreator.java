package org.pac4j.async.oauth.profile.creator;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequestAsync;
import com.github.scribejava.core.model.Verb;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.async.oauth.profile.url.OAuthProfileUrlCalculator;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.OAuth20Profile;

/**
 *
 */
public class AsyncOAuth20ProfileCreator<U extends OAuth20Profile, C extends OAuth20Configuration, PC extends OAuthProfileUrlCalculator<OAuth2AccessToken, C>> extends AsyncOAuthProfileCreator<OAuth20Credentials, U, C, OAuth2AccessToken, PC> {

    public AsyncOAuth20ProfileCreator(C configuration, PC profileUrlCalculator) {
        super(configuration, profileUrlCalculator);
    }

    @Override
    protected OAuth2AccessToken getAccessToken(OAuth20Credentials credentials) throws HttpAction {
        return credentials.getAccessToken();
    }

    @Override
    protected void signRequest(OAuth2AccessToken token, OAuthRequestAsync request) {
        this.configuration.getService().signRequest(token, request);
        if (this.configuration.isTokenAsHeader()) {
            request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BEARER_HEADER_PREFIX + token.getAccessToken());
        }
        if (Verb.POST.equals(request.getVerb())) {
            request.addParameter(OAuthConfiguration.OAUTH_TOKEN, token.getAccessToken());
        }
    }

    @Override
    protected void addAccessTokenToProfile(U profile, OAuth2AccessToken accessToken) {
        if (profile != null) {
            final String token = accessToken.getAccessToken();
            logger.debug("add access_token: {} to profile", token);
            profile.setAccessToken(token);
        }
    }
}

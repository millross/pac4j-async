package org.pac4j.async.oauth.client;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.pac4j.async.core.client.AsyncIndirectClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.async.oauth.credentials.authenticator.AsyncOAuth20Authenticator;
import org.pac4j.async.oauth.credentials.extractor.AsyncOAuth20CredentialsExtractor;
import org.pac4j.async.oauth.profile.creator.AsyncOAuth20ProfileCreator;
import org.pac4j.async.oauth.profile.url.OAuthProfileUrlCalculator;
import org.pac4j.async.oauth.redirect.AsyncOauth20RedirectActionBuilder;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.OAuth20Profile;

/**
 *
 */
public class AsyncOAuth20Client<U extends OAuth20Profile, C extends OAuth20Configuration, UC extends OAuthProfileUrlCalculator<OAuth2AccessToken, C>> extends AsyncIndirectClient<OAuth20Credentials, U> {

    protected C configuration = null;
    protected OAuthProfileUrlCalculator<OAuth2AccessToken, C> profileUrlCalculator;

    public OAuth20Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final C configuration) {
        CommonHelper.assertNotNull("configuration", configuration);
        this.configuration = configuration;
    }

    @Override
    protected void clientInit(AsyncWebContext context) {
        defaultRedirectActionBuilder(new AsyncOauth20RedirectActionBuilder(configuration));
        defaultCredentialsExtractor(new AsyncOAuth20CredentialsExtractor(configuration));
        defaultAuthenticator(new AsyncOAuth20Authenticator(configuration));
        defaultProfileCreator(new AsyncOAuth20ProfileCreator<>(configuration, profileUrlCalculator));
        this.configuration.setClientName(this.getName());
        this.configuration.setCallbackUrl(this.getCallbackUrl());
        this.configuration.setUrlResolver(this.getUrlResolver());
    }
}

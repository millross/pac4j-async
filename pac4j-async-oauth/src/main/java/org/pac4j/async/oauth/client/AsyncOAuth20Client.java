package org.pac4j.async.oauth.client;

import org.pac4j.async.core.client.AsyncIndirectClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.async.oauth.credentials.authenticator.AsyncOAuth20Authenticator;
import org.pac4j.async.oauth.credentials.extractor.AsyncOAuth20CredentialsExtractor;
import org.pac4j.async.oauth.profile.creator.AsyncOAuth20ProfileCreator;
import org.pac4j.async.oauth.redirect.AsyncOauth20RedirectActionBuilder;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.OAuth20Profile;

/**
 *
 */
public class AsyncOAuth20Client<U extends OAuth20Profile> extends AsyncIndirectClient<OAuth20Credentials, U> {

    protected OAuth20Configuration configuration = new OAuth20Configuration();

    public OAuth20Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final OAuth20Configuration configuration) {
        CommonHelper.assertNotNull("configuration", configuration);
        this.configuration = configuration;
    }

    @Override
    protected void clientInit(AsyncWebContext context) {
        defaultRedirectActionBuilder(new AsyncOauth20RedirectActionBuilder(configuration));
        defaultCredentialsExtractor(new AsyncOAuth20CredentialsExtractor(configuration));
        defaultAuthenticator(new AsyncOAuth20Authenticator(configuration));
        defaultProfileCreator(new AsyncOAuth20ProfileCreator<>(configuration));
        this.configuration.setClientName(this.getName());
        this.configuration.setCallbackUrl(this.getCallbackUrl());
        this.configuration.setUrlResolver(this.getUrlResolver());
    }
}

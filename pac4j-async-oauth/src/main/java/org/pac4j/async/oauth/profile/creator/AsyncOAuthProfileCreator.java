package org.pac4j.async.oauth.profile.creator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequestAsync;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.creator.AsyncProfileCreator;
import org.pac4j.async.oauth.config.OAuthConfiguration;
import org.pac4j.async.oauth.profile.definition.OAuthProfileDefinition;
import org.pac4j.async.oauth.scribe.ScribeCallbackAdapter;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.InitializableWebObject;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Async version of OAuthProfileCreator
 */
public abstract class AsyncOAuthProfileCreator<C extends OAuthCredentials, U extends CommonProfile, O extends OAuthConfiguration, T extends Token>
        extends InitializableWebObject<WebContext<?>> implements AsyncProfileCreator<C, U> {

    protected final static Logger logger = LoggerFactory.getLogger(AsyncOAuthProfileCreator.class);

    protected static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected final O configuration;

    protected AsyncOAuthProfileCreator(final O configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void internalInit(final WebContext<?> context) {
        CommonHelper.assertNotNull("configuration", this.configuration);
        configuration.init(context);
    }

    @Override
    public CompletableFuture<U> create(final C credentials, final AsyncWebContext context) {
        try {
            final CompletableFuture<T> tokenFuture = new CompletableFuture<>();
            try {
                tokenFuture.complete(getAccessToken(credentials));
            } catch (HttpAction action) {
                tokenFuture.completeExceptionally(action);
            }
            return tokenFuture.thenCompose(this::retrieveUserProfileFromToken);
        } catch (final OAuthException e) {
            throw new TechnicalException(e);
        }
    }

    /**
     * Retrieve the user profile from the access token.
     *
     * @param accessToken the access token
     * @return the user profile
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected CompletableFuture<U> retrieveUserProfileFromToken(final T accessToken) throws HttpAction {
        final OAuthProfileDefinition<U, T, O> profileDefinition = configuration.getProfileDefinition();
        final String profileUrl = profileDefinition.getProfileUrl(accessToken, configuration);
        final CompletableFuture<String> bodyFuture = sendRequestForData(accessToken, profileUrl, profileDefinition.getProfileVerb());
        return bodyFuture.thenApply(body -> {
            logger.info("UserProfile: " + body);
            if (body == null) {
                throw new HttpCommunicationException("No data found for accessToken: " + accessToken);
            }
            final U profile = (U) configuration.getProfileDefinition().extractUserProfile(body);
            addAccessTokenToProfile(profile, accessToken);
            return profile;
        });
    }

    /**
     * Make a request to get the data of the authenticated user for the provider.
     *
     * @param accessToken the access token
     * @param dataUrl     url of the data
     * @param verb        method used to request data
     * @return the user data response
     */
    protected CompletableFuture<String> sendRequestForData(final T accessToken, final String dataUrl, Verb verb) {
        logger.debug("accessToken: {} / dataUrl: {}", accessToken, dataUrl);
        final long t0 = System.currentTimeMillis();
        final OAuthRequestAsync request = createOAuthRequest(dataUrl, verb);
        signRequest(accessToken, request);
        final CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        request.sendAsync(ScribeCallbackAdapter.toScribeOAuthRequestCallback(responseFuture));
        return responseFuture.thenApply(response -> {
            final int code = response.getCode();
            final String body;
            try {
                body = response.getBody();
            } catch (final IOException ex) {
                throw new HttpCommunicationException("Error getting body: " + ex.getMessage());
            }
            final long t1 = System.currentTimeMillis();
            logger.debug("Request took: " + (t1 - t0) + " ms for: " + dataUrl);
            logger.debug("response code: {} / response body: {}", code, body);
            if (code != 200) {
                throw new HttpCommunicationException(code, body);
            }
            return body;
        });
    }

    /**
     * Create an OAuth request.
     *
     * @param url the url to call
     * @param verb method used to create the request
     * @return the request
     */
    protected OAuthRequestAsync createOAuthRequest(final String url, final Verb verb) {
        return new OAuthRequestAsync(verb, url, this.configuration.getService());
    }

    /**
     * Get the access token from OAuth credentials.
     *
     * @param credentials credentials
     * @return the access token
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected abstract T getAccessToken(final C credentials) throws HttpAction;

    /**
     * Sign the request.
     *
     * @param token the token
     * @param request the request
     */
    protected abstract void signRequest(final T token, final OAuthRequestAsync request);

    /**
     * Add the access token to the profile (as an attribute).
     *
     * @param profile     the user profile
     * @param accessToken the access token
     */
    protected abstract void addAccessTokenToProfile(final U profile, final T accessToken);

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "configuration", this.configuration);
    }


}

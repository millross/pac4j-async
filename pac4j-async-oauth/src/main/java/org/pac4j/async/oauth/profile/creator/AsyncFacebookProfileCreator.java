package org.pac4j.async.oauth.profile.creator;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import org.pac4j.async.oauth.config.FacebookConfiguration;
import org.pac4j.async.oauth.profile.definition.FacebookProfileDefinition;
import org.pac4j.async.oauth.profile.url.FacebookProfileUrlCalculator;
import org.pac4j.async.oauth.scribe.ScribeCallbackAdapter;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.profile.facebook.FacebookProfile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AsyncFacebookProfileCreator extends AsyncOAuth20ProfileCreator<FacebookProfile, FacebookConfiguration, FacebookProfileUrlCalculator> {

    private static final String EXCHANGE_TOKEN_URL = "https://graph.facebook.com/v2.8/oauth/access_token?grant_type=fb_exchange_token";

    private static final String EXCHANGE_TOKEN_PARAMETER = "fb_exchange_token";

    public AsyncFacebookProfileCreator(final FacebookConfiguration configuration) {
        super(configuration, new FacebookProfileUrlCalculator());
    }

    @Override
    protected CompletableFuture<FacebookProfile> retrieveUserProfileFromToken(OAuth2AccessToken accessToken) {
        final FacebookProfileDefinition profileDefinition = configuration.getProfileDefinition();
        final FacebookConfiguration facebookConfiguration = configuration;
        final String profileUrl = oAuthProfileUrlCalculator.getProfileUrl(accessToken, configuration);
        final OAuthService<OAuth2AccessToken> service = this.configuration.getService();
        CompletableFuture<String> bodyFuture = sendRequestForData(accessToken, profileUrl, Verb.GET);
        return bodyFuture.thenApply(body -> {
            if (body == null) {
                throw new HttpCommunicationException("Not data found for accessToken: " + accessToken);
            }
            return profileDefinition.extractUserProfile(body);
        }).thenApply(profile -> {
            addAccessTokenToProfile(profile, accessToken);
            return profile;
        }).thenCompose(profile -> { // May need to switch this to thenCompose
            if (profile != null && configuration.isRequiresExtendedToken()) {
                String url = CommonHelper.addParameter(EXCHANGE_TOKEN_URL, OAuthConstants.CLIENT_ID, configuration.getKey());
                url = CommonHelper.addParameter(url, OAuthConstants.CLIENT_SECRET, configuration.getSecret());
                final String finalUrl = addExchangeToken(url, accessToken);
                final OAuthRequestAsync request = createOAuthRequest(url, Verb.GET);
                final long t0 = System.currentTimeMillis();
                final CompletableFuture<Response> responseFuture = new CompletableFuture<>();
                request.sendAsync(ScribeCallbackAdapter.toScribeOAuthRequestCallback(responseFuture));
                return responseFuture.thenApply(response -> {
                    final int code = response.getCode();
                    try {
                        final String body = response.getBody();
                        final long t1 = System.currentTimeMillis();
                        logger.debug("Request took: " + (t1 - t0) + " ms for: " + finalUrl);
                        logger.debug("response code: {} / response body: {}", code, body);
                        if (code == 200) {
                            logger.debug("Retrieve extended token from  {}", body);
                            final OAuth2AccessToken extendedAccessToken;
                            try {
                                extendedAccessToken = ((DefaultApi20) configuration.getApi()).getAccessTokenExtractor().extract(response);
                            } catch (IOException | OAuthException ex) {
                                throw new HttpCommunicationException("Error extracting token: " + ex.getMessage());
                            }
                            logger.debug("Extended token: {}", extendedAccessToken);
                            addAccessTokenToProfile(profile, extendedAccessToken);
                        } else {
                            logger.error("Cannot get extended token: {} / {}", code, body);
                        }
                        return profile;
                    } catch (IOException ex) {
                        throw new HttpCommunicationException("Error getting body:" + ex.getMessage());
                    }
                });
            } else {
                return CompletableFuture.completedFuture(profile);
            }
        });

    }

    /**
     * Adds the token to the URL in question. If we require appsecret_proof, then this method
     * will also add the appsecret_proof parameter to the URL, as Facebook expects.
     *
     * @param url the URL to modify
     * @param accessToken the token we're passing back and forth
     * @return url with additional parameter(s)
     */
    protected String addExchangeToken(final String url, final OAuth2AccessToken accessToken) {
        final FacebookProfileDefinition profileDefinition = configuration.getProfileDefinition();
        String computedUrl = url;
        if (configuration.isUseAppsecretProof()) {
            computedUrl = oAuthProfileUrlCalculator.computeAppSecretProof(computedUrl, accessToken, configuration);
        }
        return CommonHelper.addParameter(computedUrl, EXCHANGE_TOKEN_PARAMETER, accessToken.getAccessToken());
    }

}

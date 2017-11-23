package org.pac4j.async.oauth.profile.url;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.pac4j.async.oauth.config.FacebookConfiguration;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.async.oauth.facebook.FacebookConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 */
public class FacebookProfileUrlCalculator extends  OAuthProfileUrlCalculator<OAuth2AccessToken, FacebookConfiguration> {

    protected static final String BASE_URL = "https://graph.facebook.com/v2.8/me";

    protected static final String APPSECRET_PARAMETER = "appsecret_proof";

    @Override
    public String getProfileUrl(final OAuth2AccessToken accessToken, final FacebookConfiguration configuration) {
        String url = BASE_URL + "?fields=" + configuration.getFields();
        if (configuration.getLimit() > FacebookConstants.DEFAULT_LIMIT) {
            url += "&limit=" + configuration.getLimit();
        }
        // possibly include the appsecret_proof parameter
        if (configuration.isUseAppsecretProof()) {
            url = computeAppSecretProof(url, accessToken, configuration);
        }
        return url;
    }

    /**
     * The code in this method is based on this blog post: https://www.sammyk.me/the-single-most-important-way-to-make-your-facebook-app-more-secure
     * and this answer: https://stackoverflow.com/questions/7124735/hmac-sha256-algorithm-for-signature-calculation
     *
     * @param url the URL to which we're adding the proof
     * @param token the application token we pass back and forth
     * @param configuration the current configuration
     * @return URL with the appsecret_proof parameter added
     */
    public String computeAppSecretProof(final String url, final OAuth2AccessToken token, final OAuth20Configuration configuration) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(configuration.getSecret().getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String proof = org.apache.commons.codec.binary.Hex.encodeHexString(sha256_HMAC.doFinal(token.getAccessToken().getBytes("UTF-8")));
            final String computedUrl = CommonHelper.addParameter(url, APPSECRET_PARAMETER, proof);
            return computedUrl;
        } catch (final Exception e) {
            throw new TechnicalException("Unable to compute appsecret_proof", e);
        }
    }


}

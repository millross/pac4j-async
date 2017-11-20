package org.pac4j.async.oauth.profile.url;

import com.github.scribejava.core.model.Token;
import org.pac4j.async.oauth.config.OAuthConfiguration;

/**
 * Base class for determining profile url for a given profile type. Extracted due to cyclic type coupling ensuring that
 * run-time type checking was required rather than improving things with a compile-time check with proper generic
 * type parameterization
 */
public abstract class OAuthProfileUrlCalculator<T extends Token, C extends OAuthConfiguration> {
    /**
     * Retrieve the url of the profile of the authenticated user for the provider.
     *
     * @param accessToken only used when constructing dynamic urls from data in the token
     * @param configuration the current configuration
     * @return the url of the user profile given by the provider
     */
    public abstract String getProfileUrl(T accessToken, C configuration);

}

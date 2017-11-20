package org.pac4j.async.oauth.profile.definition;

import com.github.scribejava.core.model.Verb;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;

import java.util.function.Function;

/**
 * OAuth profile definition.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public abstract class OAuthProfileDefinition<P extends CommonProfile> extends CommonProfileDefinition<P> {

    public OAuthProfileDefinition() {
        super();
    }

    public OAuthProfileDefinition(final Function<Object[], P> profileFactory) {
        super(profileFactory);
    }

    /**
     * Get HTTP Method to request profile.
     *
     * @return http verb
     */
    public Verb getProfileVerb() {
        return Verb.GET;
    }

    /**
     * Extract the user profile from the response (JSON, XML...) of the profile url.
     *
     * @param body the response body
     * @return the returned profile
     * @throws HttpAction whether an extra HTTP action is required
     */
    public abstract P extractUserProfile(String body) throws HttpAction;
}

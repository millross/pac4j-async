package org.pac4j.async.oauth.profile.definition;

import org.pac4j.oauth.profile.OAuth20Profile;

import java.util.function.Function;

/**
 * OAuth 2.0 profile definition.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public abstract class OAuth20ProfileDefinition<P extends OAuth20Profile> extends OAuthProfileDefinition<P> {

    public OAuth20ProfileDefinition() {
        super();
    }

    public OAuth20ProfileDefinition(final Function<Object[], P> profileFactory) {
        super(profileFactory);
    }
}

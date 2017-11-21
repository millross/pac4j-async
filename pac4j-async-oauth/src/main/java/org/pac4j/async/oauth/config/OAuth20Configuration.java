package org.pac4j.async.oauth.config;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.pac4j.async.oauth.profile.definition.OAuthProfileDefinition;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class OAuth20Configuration<U extends CommonProfile,
        P extends OAuthProfileDefinition<U>> extends OAuthConfiguration<U, P, OAuth20Service, OAuth2AccessToken> {

    public static final String OAUTH_CODE = "code";

    public static final String STATE_REQUEST_PARAMETER = "state";

    private static final String STATE_SESSION_PARAMETER = "#oauth20StateParameter";

    /* Map containing user defined parameters */
    private Map<String, String> customParams = new HashMap<>();

    private boolean withState;

    private String stateData;

    public String getStateSessionAttributeName() {
        return getClientName() + STATE_SESSION_PARAMETER;
    }

    public Map<String, String> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(final Map<String, String> customParams) {
        this.customParams = customParams;
    }

    public boolean isWithState() {
        return withState;
    }

    public void setWithState(final boolean withState) {
        this.withState = withState;
    }

    public String getStateData() {
        return stateData;
    }

    public void setStateData(final String stateData) {
        this.stateData = stateData;
    }

    @Override
    public OAuth20Service getService() {
        return super.getService();
    }

    @Override
    public OAuth20Service buildService(WebContext<?> context, String state) {
        return super.buildService(context, state);
    }
}

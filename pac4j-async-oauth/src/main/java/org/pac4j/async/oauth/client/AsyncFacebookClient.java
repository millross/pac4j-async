package org.pac4j.async.oauth.client;

import com.github.scribejava.apis.FacebookApi;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.oauth.config.FacebookConfiguration;
import org.pac4j.async.oauth.facebook.FacebookConstants;
import org.pac4j.async.oauth.profile.creator.AsyncFacebookProfileCreator;
import org.pac4j.async.oauth.profile.definition.FacebookProfileDefinition;
import org.pac4j.async.oauth.profile.url.FacebookProfileUrlCalculator;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.exception.OAuthCredentialsException;
import org.pac4j.oauth.profile.facebook.FacebookProfile;

/**
 *
 */
public class AsyncFacebookClient extends AsyncOAuth20Client<FacebookProfile, FacebookConfiguration, FacebookProfileUrlCalculator> {

    public final static String DEFAULT_FIELDS = "id,name,first_name,middle_name,last_name,gender,locale,languages,link,third_party_id,timezone,updated_time,verified,about,birthday,education,email,hometown,interested_in,location,political,favorite_athletes,favorite_teams,quotes,relationship_status,religion,significant_other,website,work";

    protected String fields = DEFAULT_FIELDS;

    public final static String DEFAULT_SCOPE = "user_likes,user_about_me,user_birthday,user_education_history,email,user_hometown,user_relationship_details,user_location,user_religion_politics,user_relationships,user_website,user_work_history";

    protected String scope = DEFAULT_SCOPE;

    protected int limit = FacebookConstants.DEFAULT_LIMIT;

    protected boolean requiresExtendedToken = false;

    protected boolean useAppsecretProof = false;

    public AsyncFacebookClient() {
    }

    public AsyncFacebookClient(final String key, final String secret) {
        setKey(key);
        setSecret(secret);
    }

    @Override
    protected void clientInit(final AsyncWebContext context) {
        CommonHelper.assertNotBlank("fields", this.fields);
        configuration.setApi(FacebookApi.instance());
        configuration.setProfileDefinition(new FacebookProfileDefinition());
        configuration.setScope(scope);
        configuration.setHasBeenCancelledFactory(ctx -> {
            final String error = ctx.getRequestParameter(OAuthCredentialsException.ERROR);
            final String errorReason = ctx.getRequestParameter(OAuthCredentialsException.ERROR_REASON);
            // user has denied permissions
            if ("access_denied".equals(error) && "user_denied".equals(errorReason)) {
                return true;
            } else {
                return false;
            }
        });
        configuration.setWithState(true);
        setConfiguration(configuration);
        defaultProfileCreator(new AsyncFacebookProfileCreator(configuration));

        super.clientInit(context);
    }

    public void setStateData(final String stateData) {
        configuration.setStateData(stateData);
    }

    public String getStateData() {
        return configuration.getStateData();
    }

    public void setUseAppSecretProof(final boolean useSecret) {
        this.useAppsecretProof = useSecret;
    }

    public boolean getUseAppSecretProof() {
        return this.useAppsecretProof;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getFields() {
        return this.fields;
    }

    public void setFields(final String fields) {
        this.fields = fields;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public boolean isRequiresExtendedToken() {
        return this.requiresExtendedToken;
    }

    public void setRequiresExtendedToken(final boolean requiresExtendedToken) {
        this.requiresExtendedToken = requiresExtendedToken;
    }
}

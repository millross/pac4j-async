package org.pac4j.async.core.client;

import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuth;
import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuthenticationStrategy;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.AjaxRequestResolver;
import org.pac4j.core.http.DefaultAjaxRequestResolver;
import org.pac4j.core.http.DefaultUrlResolver;
import org.pac4j.core.http.UrlResolver;
import org.pac4j.core.logout.LogoutActionBuilder;
import org.pac4j.core.logout.NoLogoutActionBuilder;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.core.redirect.RedirectActionBuilder;
import org.pac4j.core.util.CommonHelper;

/**
 *
 */
public abstract class AsyncIndirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {

    private RedirectActionBuilder redirectActionBuilder;


    protected String callbackUrl;
    private boolean includeClientNameInCallbackUrl = true;
    protected UrlResolver urlResolver = new DefaultUrlResolver();
    private AjaxRequestResolver ajaxRequestResolver = new DefaultAjaxRequestResolver();
    private LogoutActionBuilder<U> logoutActionBuilder = NoLogoutActionBuilder.INSTANCE;

    public RedirectAction getRedirectAction(final AsyncWebContext context) throws HttpAction {
        return null;
    }

    @Override
    protected void internalInit(AsyncWebContext context) {

        CommonHelper.assertNotBlank("callbackUrl", this.callbackUrl,
                "set it up either on this IndirectClient or the global Config");
        CommonHelper.assertNotNull("urlResolver", this.urlResolver);
        CommonHelper.assertNotNull("ajaxRequestResolver", this.ajaxRequestResolver);

        clientInit(context);

        // ensures components have been properly initialized
        CommonHelper.assertNotNull("redirectActionBuilder", this.redirectActionBuilder);
        CommonHelper.assertNotNull("credentialsExtractor", getCredentialsExtractor());
        CommonHelper.assertNotNull("authenticator", getAuthenticator());
        CommonHelper.assertNotNull("profileCreator", getProfileCreator());
        CommonHelper.assertNotNull("logoutActionBuilder", this.logoutActionBuilder);

    }

    /**
     * Initialize the client.
     *
     * @param context the web context
     */
    protected abstract void clientInit(WebContext context);

    @Override
    protected final RecordFailedAuthenticationStrategy authFailureRecorder() {
        return RecordFailedAuth.RECORD_IN_SESSION;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public boolean isIncludeClientNameInCallbackUrl() {
        return includeClientNameInCallbackUrl;
    }

    public void setIncludeClientNameInCallbackUrl(boolean includeClientNameInCallbackUrl) {
        this.includeClientNameInCallbackUrl = includeClientNameInCallbackUrl;
    }

    public UrlResolver getUrlResolver() {
        return urlResolver;
    }

    public void setUrlResolver(UrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    public AjaxRequestResolver getAjaxRequestResolver() {
        return ajaxRequestResolver;
    }

    public void setAjaxRequestResolver(AjaxRequestResolver ajaxRequestResolver) {
        this.ajaxRequestResolver = ajaxRequestResolver;
    }

    public RedirectActionBuilder getRedirectActionBuilder() {
        return redirectActionBuilder;
    }

    public void setRedirectActionBuilder(RedirectActionBuilder redirectActionBuilder) {
        this.redirectActionBuilder = redirectActionBuilder;
    }

    public LogoutActionBuilder<U> getLogoutActionBuilder() {
        return logoutActionBuilder;
    }

    public void setLogoutActionBuilder(LogoutActionBuilder<U> logoutActionBuilder) {
        this.logoutActionBuilder = logoutActionBuilder;
    }
}


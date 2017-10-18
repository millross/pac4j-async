package org.pac4j.async.core.client;

import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuth;
import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuthenticationStrategy;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
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

import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<RedirectAction> getRedirectAction(final AsyncWebContext context) throws HttpAction {
        init(context);
        // it's an AJAX request -> unauthorized (with redirection url in header)
        if (ajaxRequestResolver.isAjax(context)) {
            logger.info("AJAX request detected -> returning 401");
            final RedirectAction action = redirectActionBuilder.redirect(context);
            return cleanRequestedUrl(context)
                    .thenApply(v -> {
                        final String url = action.getLocation();
                        if (CommonHelper.isNotBlank(url)) {
                            context.setResponseHeader(HttpConstants.LOCATION_HEADER, url);
                        }
                        throw HttpAction.unauthorized("AJAX request -> 401", context, null);
                    });
        }

        // authentication has already been tried -> unauthorized
        return authFailureRecorder().isFailedAuthenticationPresent(this, context)
                .thenCompose(authAttempted -> {
                   if (authAttempted) {
                       return authFailureRecorder().clearFailedAuthentication(this, context)
                               .thenCompose(v -> cleanRequestedUrl(context))
                               .thenApply(v -> {
                                   throw HttpAction.unauthorized("authentication already tried -> forbidden", context, null);
                               });
                   } else {
                       return CompletableFuture.completedFuture(redirectActionBuilder.redirect(context));
                   }
                });

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
    protected abstract void clientInit(AsyncWebContext context);

    @Override
    protected final RecordFailedAuthenticationStrategy authFailureRecorder() {
        return RecordFailedAuth.RECORD_IN_SESSION;
    }

    private CompletableFuture<Void> cleanRequestedUrl(final AsyncWebContext context) {
        return context.getSessionStore().set(context, Pac4jConstants.REQUESTED_URL, "");
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


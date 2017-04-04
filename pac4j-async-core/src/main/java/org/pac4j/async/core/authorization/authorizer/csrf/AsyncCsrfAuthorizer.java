package org.pac4j.async.core.authorization.authorizer.csrf;

import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.ContextHelper;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AsyncCsrfAuthorizer implements AsyncAuthorizer<CommonProfile> {

    private String parameterName = Pac4jConstants.CSRF_TOKEN;

    private String headerName = Pac4jConstants.CSRF_TOKEN;

    private boolean onlyCheckPostRequest = true;

    public AsyncCsrfAuthorizer() {
    }

    public AsyncCsrfAuthorizer(final String parameterName, final String headerName) {
        this.parameterName = parameterName;
        this.headerName = headerName;
    }

    public AsyncCsrfAuthorizer(final String parameterName, final String headerName, final boolean onlyCheckPostRequest) {
        this(parameterName, headerName);
        this.onlyCheckPostRequest = onlyCheckPostRequest;
    }

    @Override
    public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
        final boolean checkRequest = !onlyCheckPostRequest || ContextHelper.isPost(context);
        if (checkRequest) {
            final String parameterToken = context.getRequestParameter(parameterName);
            final String headerToken = context.getRequestHeader(headerName);
            return context.getSessionAttribute(Pac4jConstants.CSRF_TOKEN)
                    .thenApply(s -> s != null && (s.equals(parameterToken) || s.equals(headerToken)));
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isOnlyCheckPostRequest() {
        return onlyCheckPostRequest;
    }

    public void setOnlyCheckPostRequest(boolean onlyCheckPostRequest) {
        this.onlyCheckPostRequest = onlyCheckPostRequest;
    }

}

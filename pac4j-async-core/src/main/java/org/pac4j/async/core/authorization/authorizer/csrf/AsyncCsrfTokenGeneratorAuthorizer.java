package org.pac4j.async.core.authorization.authorizer.csrf;

import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AsyncCsrfTokenGeneratorAuthorizer implements AsyncAuthorizer<CommonProfile>{

    private AsyncCsrfTokenGenerator csrfTokenGenerator;

    private String domain;

    private String path = "/";

    private Boolean httpOnly;

    private Boolean secure;

    public AsyncCsrfTokenGeneratorAuthorizer(final AsyncCsrfTokenGenerator csrfTokenGenerator) {
        this.csrfTokenGenerator = csrfTokenGenerator;
    }

    @Override
    public CompletableFuture<Boolean> isAuthorized(final AsyncWebContext<?> context, final List<CommonProfile> profiles) {
        CommonHelper.assertNotNull("csrfTokenGenerator", csrfTokenGenerator);
        final CompletableFuture<String> tokenFuture = csrfTokenGenerator.get(context);
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        tokenFuture.thenAccept(token -> context.getExecutionContext().runOnContext(() -> {
            context.setRequestAttribute(Pac4jConstants.CSRF_TOKEN, token);
            final Cookie cookie = new Cookie(Pac4jConstants.CSRF_TOKEN, token);
            if (domain != null) {
                cookie.setDomain(domain);
            } else {
                cookie.setDomain(context.getServerName());
            }
            if (path != null) {
                cookie.setPath(path);
            }
            if (httpOnly != null) {
                cookie.setHttpOnly(httpOnly.booleanValue());
            }
            if (secure != null) {
                cookie.setSecure(secure.booleanValue());
            }
            context.addResponseCookie(cookie);
            result.complete(true);
        }));
        return result;

    }

    public AsyncCsrfTokenGenerator getCsrfTokenGenerator() {
        return csrfTokenGenerator;
    }

    public void setCsrfTokenGenerator(final AsyncCsrfTokenGenerator csrfTokenGenerator) {
        this.csrfTokenGenerator = csrfTokenGenerator;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(final Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(final Boolean secure) {
        this.secure = secure;
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "csrfTokenGenerator", csrfTokenGenerator, "domain", domain, "path", path, "httpOnly", httpOnly, "secure", secure);
    }

}

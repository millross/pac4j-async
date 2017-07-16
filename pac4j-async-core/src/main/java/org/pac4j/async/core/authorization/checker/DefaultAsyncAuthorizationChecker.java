package org.pac4j.async.core.authorization.checker;

import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.authorization.authorizer.csrf.AsyncCsrfAuthorizer;
import org.pac4j.async.core.authorization.authorizer.csrf.AsyncCsrfTokenGeneratorAuthorizer;
import org.pac4j.async.core.authorization.authorizer.csrf.DefaultAsyncCsrfTokenGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.*;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer.fromNonBlockingAuthorizer;
import static org.pac4j.async.core.future.FutureUtils.shortCircuitedFuture;
import static org.pac4j.core.util.CommonHelper.*;

/**
 *
 */
public class DefaultAsyncAuthorizationChecker implements AsyncAuthorizationChecker<CommonProfile> {

    private static final CorsAuthorizer SYNC_CORS_AUTHORIZER = new CorsAuthorizer();

    final static AsyncAuthorizer<CommonProfile> STRICT_TRANSPORT_SECURITY_HEADER = fromNonBlockingAuthorizer(new StrictTransportSecurityHeader());
    final static AsyncAuthorizer<CommonProfile> X_CONTENT_TYPE_OPTIONS_HEADER = fromNonBlockingAuthorizer(new XContentTypeOptionsHeader());
    final static AsyncAuthorizer<CommonProfile> X_FRAME_OPTIONS_HEADER = fromNonBlockingAuthorizer(new XFrameOptionsHeader());
    final static AsyncAuthorizer<CommonProfile> XSS_PROTECTION_HEADER = fromNonBlockingAuthorizer(new XSSProtectionHeader());
    final static AsyncAuthorizer<CommonProfile> CACHE_CONTROL_HEADER = fromNonBlockingAuthorizer(new CacheControlHeader());
    final static AsyncAuthorizer<CommonProfile> CSRF_AUTHORIZER = new AsyncCsrfAuthorizer();
    final static AsyncAuthorizer<CommonProfile> CSRF_TOKEN_GENERATOR_AUTHORIZER = new AsyncCsrfTokenGeneratorAuthorizer(new DefaultAsyncCsrfTokenGenerator());
    final static AsyncAuthorizer<CommonProfile> CORS_AUTHORIZER = fromNonBlockingAuthorizer(SYNC_CORS_AUTHORIZER);
    final static AsyncAuthorizer<CommonProfile> IS_ANONYMOUS_AUTHORIZER = fromNonBlockingAuthorizer(new IsAnonymousAuthorizer());
    final static AsyncAuthorizer<CommonProfile> IS_AUTHENTICATED_AUTHORIZER = fromNonBlockingAuthorizer(new IsAuthenticatedAuthorizer());
    final static AsyncAuthorizer<CommonProfile> IS_FULLY_AUTHENTICATED_AUTHORIZER = fromNonBlockingAuthorizer(new IsFullyAuthenticatedAuthorizer());
    final static AsyncAuthorizer<CommonProfile> IS_REMEMBERED_AUTHORIZER = fromNonBlockingAuthorizer(new IsRememberedAuthorizer());

    // Configure underlying authorizer as we want it to behave
    static {
        SYNC_CORS_AUTHORIZER.setAllowOrigin("*");
        SYNC_CORS_AUTHORIZER.setAllowCredentials(true);
        final Set<HttpConstants.HTTP_METHOD> methods = new HashSet<>();
        methods.add(HttpConstants.HTTP_METHOD.GET);
        methods.add(HttpConstants.HTTP_METHOD.PUT);
        methods.add(HttpConstants.HTTP_METHOD.POST);
        methods.add(HttpConstants.HTTP_METHOD.DELETE);
        methods.add(HttpConstants.HTTP_METHOD.OPTIONS);
        SYNC_CORS_AUTHORIZER.setAllowMethods(methods);
    }



    @Override
    public CompletableFuture<Boolean> isAuthorized(final AsyncWebContext context, final List<CommonProfile> profiles,
                                                   final String authorizerNames,
                                                   final Map<String, AsyncAuthorizer> authorizersMap) {

        final List<AsyncAuthorizer> authorizers = new LinkedList<>();

        // if we have an authorizer name (which may be a list of authorizer names)
        if (isNotBlank(authorizerNames)) {
            final String[] names = authorizerNames.split(Pac4jConstants.ELEMENT_SEPRATOR);
            final int nb = names.length;
            for (int i = 0; i < nb; i++) {
                final String name = names[i].trim();
                if ("hsts".equalsIgnoreCase(name)) {
                    authorizers.add(STRICT_TRANSPORT_SECURITY_HEADER);
                } else if ("nosniff".equalsIgnoreCase(name)) {
                    authorizers.add(X_CONTENT_TYPE_OPTIONS_HEADER);
                } else if ("noframe".equalsIgnoreCase(name)) {
                    authorizers.add(X_FRAME_OPTIONS_HEADER);
                } else if ("xssprotection".equalsIgnoreCase(name)) {
                    authorizers.add(XSS_PROTECTION_HEADER);
                } else if ("nocache".equalsIgnoreCase(name)) {
                    authorizers.add(CACHE_CONTROL_HEADER);
                } else if ("securityheaders".equalsIgnoreCase(name)) {
                    authorizers.add(CACHE_CONTROL_HEADER);
                    authorizers.add(X_CONTENT_TYPE_OPTIONS_HEADER);
                    authorizers.add(STRICT_TRANSPORT_SECURITY_HEADER);
                    authorizers.add(X_FRAME_OPTIONS_HEADER);
                    authorizers.add(XSS_PROTECTION_HEADER);
                } else if ("csrfToken".equalsIgnoreCase(name)) {
                    authorizers.add(CSRF_TOKEN_GENERATOR_AUTHORIZER);
                } else if ("csrfCheck".equalsIgnoreCase(name)) {
                    authorizers.add(CSRF_AUTHORIZER);
                } else if ("csrf".equalsIgnoreCase(name)) {
                    authorizers.add(CSRF_TOKEN_GENERATOR_AUTHORIZER);
                    authorizers.add(CSRF_AUTHORIZER);
                } else if ("allowAjaxRequests".equalsIgnoreCase(name)) {
                    authorizers.add(CORS_AUTHORIZER);
                } else if ("isAnonymous".equalsIgnoreCase(name)) {
                    authorizers.add(IS_ANONYMOUS_AUTHORIZER);
                } else if ("isAuthenticated".equalsIgnoreCase(name)) {
                    authorizers.add(IS_AUTHENTICATED_AUTHORIZER);
                } else if ("isFullyAuthenticated".equalsIgnoreCase(name)) {
                    authorizers.add(IS_FULLY_AUTHENTICATED_AUTHORIZER);
                } else if ("isRemembered".equalsIgnoreCase(name)) {
                    authorizers.add(IS_REMEMBERED_AUTHORIZER);
                } else {
                    // we must have authorizers
                    assertNotNull("authorizersMap", authorizersMap);
                    AsyncAuthorizer result = null;
                    for (final Map.Entry<String, AsyncAuthorizer> entry : authorizersMap.entrySet()) {
                        if (areEqualsIgnoreCaseAndTrim(entry.getKey(), name)) {
                            result = entry.getValue();
                            break;
                        }
                    }
                    // we must have an authorizer defined for this name
                    assertNotNull("authorizersMap['" + name + "']", result);
                    authorizers.add(result);
                }
            }
        }
        return isAuthorized(context, profiles, authorizers);
    }

    protected CompletableFuture<Boolean> isAuthorized(final AsyncWebContext context, final List<CommonProfile> profiles,
                                                      final List<AsyncAuthorizer> authorizers) {
        // authorizations check comes after authentication and profile must not be null nor empty
        assertTrue(isNotEmpty(profiles), "profiles must not be null or empty");

        if ( isNotEmpty(authorizers)) {
            return shortCircuitedFuture(authorizers.stream()
                    .map(a -> () -> a.isAuthorized(context, profiles)), false);
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

}

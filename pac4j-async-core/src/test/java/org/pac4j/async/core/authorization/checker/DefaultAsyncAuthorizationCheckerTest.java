package org.pac4j.async.core.authorization.checker;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.authorization.authorizer.csrf.DefaultAsyncCsrfTokenGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.util.TestsConstants;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.aol.cyclops.invokedynamic.ExceptionSoftener.softenConsumer;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer.fromNonBlockingAuthorizer;
import static org.pac4j.core.context.HttpConstants.*;
import static org.pac4j.core.context.Pac4jConstants.CSRF_TOKEN;
import static org.pac4j.core.context.Pac4jConstants.ELEMENT_SEPRATOR;


/**
 *
 */
public class DefaultAsyncAuthorizationCheckerTest extends VertxAsyncTestBase implements TestsConstants {
    private final DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();

    private List<CommonProfile> profiles;

    private CommonProfile profile;

    @Before
    public void setUp() {
        profile = new CommonProfile();
        profiles = new ArrayList<>();
        profiles.add(profile);
    }

    private static class IdAuthorizer implements AsyncAuthorizer<CommonProfile> {

        @Override
        public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
            return completedFuture(VALUE.equals(profiles.get(0).getId()));
        }
    }

    @Test
    public void testBlankAuthorizerNameAProfile(final TestContext testContext) throws Exception {

        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, null, null),
                b -> assertThat(b, is(true)));
     }

    @Test(timeout = 1000)
    public void testOneExistingAuthorizerProfileMatch(final TestContext testContext) throws Exception {
        profile.setId(VALUE);
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());

        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, NAME, authorizers),
                b -> assertThat(b, is(true)));

    }

    @Test
    public void testOneExistingAuthorizerProfileDoesNotMatch(final TestContext testContext) throws Exception {
        internalTestOneExistingAuthorizerProfileDoesNotMatch(testContext, NAME);
    }

    @Test
    public void testOneExistingAuthorizerProfileDoesNotMatchCasTrim(final TestContext testContext) throws Exception {
        internalTestOneExistingAuthorizerProfileDoesNotMatch(testContext, "   NaME       ");
    }

    @Test(expected = TechnicalException.class)
    public void testOneAuthorizerDoesNotExist(final TestContext testContext) throws Exception {
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, VALUE, authorizers),
                b -> {});
    }

    @Test
    public void testTwoExistingAuthorizerProfileMatch(final TestContext testContext) throws Exception {
        profile.setId(VALUE);
        profile.addRole(ROLE);
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());
        authorizers.put(VALUE, fromNonBlockingAuthorizer(new RequireAnyRoleAuthorizer(ROLE)));

        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, NAME + ELEMENT_SEPRATOR + VALUE, authorizers),
                b -> assertThat(b, is(true)));
    }


    @Test
    public void testTwoExistingAuthorizerProfileDoesNotMatch(final TestContext testContext) throws Exception {
        profile.addRole(ROLE);
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());
        authorizers.put(VALUE, fromNonBlockingAuthorizer(new RequireAnyRoleAuthorizer(ROLE)));
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, NAME + ELEMENT_SEPRATOR + VALUE, authorizers),
                b -> assertThat(b, is(false)));
    }

    @Test(expected = TechnicalException.class)
    public void testTwoAuthorizerOneDoesNotExist(final TestContext testContext) throws Exception {
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, NAME + ELEMENT_SEPRATOR + VALUE, authorizers),
                b -> {});
    }

    @Test(expected = TechnicalException.class)
    public void testNullAuthorizers(final TestContext testContext) throws Exception {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, null),
                b -> assertThat(b, is(true)));
        checker.isAuthorized(null, profiles, "auth1", null);
    }

    @Test
    public void testZeroAuthorizers(final TestContext testContext) throws Exception  {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, new ArrayList<>()),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testEmptyAuthorizerNames(final TestContext testContext) throws Exception {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, "", new HashMap<>()),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testOneExistingAuthorizerProfileMatch2(final TestContext testContext) throws Exception {
        profile.setId(VALUE);
        final List<AsyncAuthorizer> authorizers = new ArrayList<>();
        authorizers.add(new IdAuthorizer());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, authorizers),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testOneExistingAuthorizerProfileDoesNotMatch2(final TestContext testContext) throws Exception {
        final List<AsyncAuthorizer> authorizers = new ArrayList<>();
        authorizers.add(new IdAuthorizer());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, authorizers),
                b -> assertThat(b, is(false)));
    }

    @Test
    public void testTwoExistingAuthorizerProfileMatch2(final TestContext testContext) throws Exception {
        profile.setId(VALUE);
        profile.addRole(ROLE);
        final List<AsyncAuthorizer> authorizers = new ArrayList<>();
        authorizers.add(new IdAuthorizer());
        authorizers.add(fromNonBlockingAuthorizer(new RequireAnyRoleAuthorizer(ROLE)));
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, authorizers),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testTwoExistingAuthorizerProfileDoesNotMatch2(final TestContext testContext) throws Exception {
        profile.addRole(ROLE);
        final List<AsyncAuthorizer> authorizers = new ArrayList<>();
        authorizers.add(new IdAuthorizer());
        authorizers.add(fromNonBlockingAuthorizer(new RequireAnyRoleAuthorizer(ROLE)));
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, authorizers),
                b -> assertThat(b, is(false)));
    }

    @Test(expected = TechnicalException.class)
    public void testNullProfile(final TestContext testContext) throws Exception {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, null, new ArrayList<>()),
                b -> {});
    }

    @Test
    public void testHsts(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "hsts", null),
                b -> assertThat(responseHeaders.get("Strict-Transport-Security"), is(notNullValue())));
    }

    @Test
    public void testHstsCaseTrim(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "  HSTS ", null),
                b -> assertThat(responseHeaders.get("Strict-Transport-Security"), is(notNullValue())));
    }

    @Test
    public void testNosniff(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "nosniff", null),
                b -> assertThat(responseHeaders.get("X-Content-Type-Options"), is(notNullValue())));
    }

    @Test
    public void testNoframe(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "noframe", null),
                b -> assertThat(responseHeaders.get("X-Frame-Options"), is(notNullValue())));
    }

    @Test
    public void testXssprotection(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "xssprotection", null),
                b -> assertThat(responseHeaders.get("X-XSS-Protection"), is(notNullValue())));
    }

    @Test
    public void testNocache(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "nocache", null),
                b -> {
                    assertThat(responseHeaders.get("Cache-Control"), is(notNullValue()));
                    assertThat(responseHeaders.get("Pragma"), is(notNullValue()));
                    assertThat(responseHeaders.get("Expires"), is(notNullValue()));
                });
    }

    @Test
    public void testAllowAjaxRequests(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "allowAjaxRequests", null),
                b -> {
                    assertThat(responseHeaders.get(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER), is("*"));
                    assertThat(responseHeaders.get(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER), is("true"));
                    final String methods = responseHeaders.get(ACCESS_CONTROL_ALLOW_METHODS_HEADER);
                    final List<String> methodArray = Arrays.asList(methods.split(",")).stream().map(String::trim).
                            collect(Collectors.toList());
                    assertThat(methodArray.containsAll(Arrays.asList(HTTP_METHOD.POST.name(),
                            HTTP_METHOD.PUT.name(),
                            HTTP_METHOD.DELETE.name(),
                            HTTP_METHOD.OPTIONS.name(),
                            HTTP_METHOD.GET.name())),
                            is(true));
                });
    }

    @Test
    public void testSecurityHeaders(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "securityHeaders", null),
                b -> {
                    Arrays.asList("Strict-Transport-Security",
                            "X-Content-Type-Options",
                            "X-Content-Type-Options",
                            "X-XSS-Protection",
                            "Cache-Control",
                            "Pragma",
                            "Expires"
                            ).forEach(s -> assertThat(responseHeaders.get(s), is(notNullValue())));
                });
    }

    @Test
    public void testCsrf(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "csrf", null),
                b -> {
                    assertThat(b, is(true));
                    assertThat(context.getRequestAttribute(CSRF_TOKEN), is(notNullValue()));
                    assertThat(responseCookies.get(CSRF_TOKEN), is(notNullValue()));
                });
    }

    @Test
    public void testCsrfToken(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "csrfToken", null),
                b -> {
                    assertThat(b, is(true));
                    assertThat(context.getRequestAttribute(CSRF_TOKEN), is(notNullValue()));
                    assertThat(responseCookies.get(CSRF_TOKEN), is(notNullValue()));
                });
    }

    @Test
    public void testCsrfPost(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.POST);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "csrf", null),
                b -> {
                    assertThat(b, is(false));
                    assertThat(context.getRequestAttribute(CSRF_TOKEN), is(notNullValue()));
                    assertThat(responseCookies.get(CSRF_TOKEN), is(notNullValue()));
                });
    }

    @Test
    public void testCsrfTokenPost(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.POST);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(context, profiles, "csrfToken", null),
                b -> {
                    assertThat(b, is(true));
                    assertThat(context.getRequestAttribute(CSRF_TOKEN), is(notNullValue()));
                    assertThat(responseCookies.get(CSRF_TOKEN), is(notNullValue()));
                });
    }

    @Test
    public void testCsrfPostTokenParameter(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.POST);
        programMockWebContextForSessionAttributes(context);

        final DefaultAsyncCsrfTokenGenerator generator = new DefaultAsyncCsrfTokenGenerator();
        final CompletableFuture<String> tokenFuture = generator.get(context);
        tokenFuture.thenAccept(softenConsumer(token -> {
            when(context.getRequestParameter(CSRF_TOKEN)).thenReturn(token);
            assertAuthorizationResults(testContext,
                    () -> checker.isAuthorized(context, profiles, "csrf", null),
                    b -> {
                        assertThat(b, is(true));
                        assertThat(context.getRequestAttribute(CSRF_TOKEN), is(notNullValue()));
                        assertThat(responseCookies.get(CSRF_TOKEN), is(notNullValue()));
                    });

        }));
    }

    @Test
    public void testCsrfCheckPost(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.POST);
        final DefaultAsyncCsrfTokenGenerator generator = new DefaultAsyncCsrfTokenGenerator();
        generator.get(context).thenAccept(softenConsumer(token -> {
            assertAuthorizationResults(testContext,
                    () -> checker.isAuthorized(context, profiles, "csrfCheck", null),
                    b -> assertThat(b, is(false)));

        }));
    }

    @Test
    public void testCsrfCheckPostTokenParameter(final TestContext testContext) throws Exception {
        final Map<String, String> responseHeaders = new HashMap<>();
        final Map<String, Cookie> responseCookies = new HashMap<>();
        final AsyncWebContext context = mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.POST);
        final DefaultAsyncCsrfTokenGenerator generator = new DefaultAsyncCsrfTokenGenerator();
        generator.get(context).thenAccept(softenConsumer(token -> {
            when(context.getRequestParameter(CSRF_TOKEN)).thenReturn(token);
            assertAuthorizationResults(testContext,
                    () -> checker.isAuthorized(context, profiles, "csrfCheck", null),
                    b -> assertThat(b, is(false)));
        }));
    }

    @Test
    public void testIsAnonymous(final TestContext testContext) throws Exception {
        profiles.clear();
        profiles.add(new AnonymousProfile());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, "isAnonymous", null),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testIsAuthenticated(final TestContext testContext) throws Exception {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, "isAuthenticated", null),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testIsFullyAuthenticated(final TestContext testContext) throws Exception {
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, "isFullyAuthenticated", null),
                b -> assertThat(b, is(true)));
    }

    @Test
    public void testIsRemembered(final TestContext testContext) throws Exception {
        profile.setRemembered(true);
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, "isRemembered", null),
                b -> assertThat(b, is(true)));
    }

    private void internalTestOneExistingAuthorizerProfileDoesNotMatch(final TestContext testContext, final String name) throws Exception {
        final Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put(NAME, new IdAuthorizer());
        assertAuthorizationResults(testContext,
                () -> checker.isAuthorized(null, profiles, name, authorizers),
                b -> assertThat(b, is(false)));
    }

    private void assertAuthorizationResults(final TestContext testContext,
                                            final Supplier<CompletableFuture<Boolean>> toTest,
                                            final Consumer<Boolean> assertions) throws Exception {
        final Async async = testContext.async();
        toTest.get().thenAccept(b -> executionContext.runOnContext(() -> {
            assertions.accept(b);
            async.complete();
        }));

    }

    private AsyncWebContext mockWebContext(final Map<String, String> responseHeaders) {
        return mockWebContext(responseHeaders, new HashMap<>());
    }

    private AsyncWebContext mockWebContext(Map<String, String> responseHeaders, Map<String, Cookie> responseCookies) {
        return mockWebContext(responseHeaders, responseCookies, HTTP_METHOD.GET);
    }

    private void programMockWebContextForSessionAttributes(final AsyncWebContext mockWebContext) {
        final Map<String, Object> session = new HashMap<>();

        doAnswer(invocation -> {
            final String key = invocation.getArgumentAt(0, String.class);
            return completedFuture(session.get(key));
        }).when(mockWebContext).getSessionAttribute(anyString());

        doAnswer(invocation -> {
            final String key = invocation.getArgumentAt(0, String.class);
            final Object value = invocation.getArgumentAt(1, Object.class);
            session.put(key, value);
            return completedFuture(null);
        }).when(mockWebContext).setSessionAttribute(anyString(), anyObject());
    }

    private AsyncWebContext mockWebContext(final Map<String, String> responseHeaders,
                                           final Map<String, Cookie> responseCookies,
                                           final HTTP_METHOD httpMethod) {
        final AsyncWebContext context = mock(AsyncWebContext.class);
        final Map<String, Object> requestAttributes = new HashMap<>();
        when(context.getScheme()).thenReturn(SCHEME_HTTPS);
        when(context.getFullRequestURL()).thenReturn("http://localhost:80");
        doAnswer(invocation -> {
            final String headerName = invocation.getArgumentAt(0, String.class);
            final String headerValue = invocation.getArgumentAt(1, String.class);
            responseHeaders.put(headerName, headerValue);
            return null;
        }).when(context).setResponseHeader(anyString(), anyString());
        doAnswer(invocation ->  {
            final Cookie cookie = invocation.getArgumentAt(0, Cookie.class);
            responseCookies.put(cookie.getName(), cookie);
            return null;
        }).when(context).addResponseCookie(any(Cookie.class));
        doAnswer(invocation -> {
            final String paramName = invocation.getArgumentAt(0, String.class);
            final Object paramValue = invocation.getArgumentAt(1, Object.class);
            requestAttributes.put(paramName, paramValue);
            return null;
        }).when(context).setRequestAttribute(anyString(), any());
        doAnswer(invocation -> {
            final String paramName = invocation.getArgumentAt(0, String.class);
            return requestAttributes.get(paramName);
        }).when(context).getRequestAttribute(anyString());
        when(context.getExecutionContext()).thenReturn(executionContext);
        when(context.setSessionAttribute(anyString(), any())).thenReturn(completedFuture(null));
        when(context.getSessionAttribute(anyString())).thenReturn(completedFuture(null));
        when(context.getRequestMethod()).thenReturn(httpMethod.name());
        return context;
    }

}
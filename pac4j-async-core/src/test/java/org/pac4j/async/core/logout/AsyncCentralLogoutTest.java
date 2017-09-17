package org.pac4j.async.core.logout;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.logout.AsyncCentralLogout.CENTRAL_LOGOUT;

/**
 * Tests for AsyncCentralLogoutStrategy default implementartions
 */
public class AsyncCentralLogoutTest extends VertxAsyncTestBase {

    private static final String LOGOUT_URL_1 = "http://example.com/logout1";
    private static final String LOGOUT_URL_2 = "http://example.com/logout2";
    private static final String VALID_REDIRECT_URL = "http://example.com/redirect";
    private static final String INVALID_REDIRECT_URL = "htt://example.com/redirect";

    final AsyncClient<TestCredentials, CommonProfile> client = mock(AsyncClient.class);
    final Clients<AsyncClient<? extends Credentials, ? extends CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>> clients = mock(Clients.class);
    final CommonProfile firstProfile = mock(CommonProfile.class);
    final CommonProfile secondProfile = mock(CommonProfile.class);
    private AtomicInteger status;

    AsyncWebContext webContext;
    Map<String, String> responseHeaders;

    @Before
    public void setUp() {

        responseHeaders = new HashMap<>();
        status = new AtomicInteger();
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), asynchronousComputationAdapter)
                .withStatusRecording(status)
                .withRecordedResponseHeaders(responseHeaders)
                .build();
    }

    @Test
    public void centralLogoutNullClientName() {
        when(firstProfile.getClientName()).thenReturn(null);
        final Optional<Supplier<HttpAction>> centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile), null, null);
        assertThat(centralLogoutAction.isPresent(), is(false));
        verify(clients, never()).findClient(anyString());
        verify(client, never()).getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), anyString());
    }

    @Test
    public void centralLogoutNullClient() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(null);
        final Optional<Supplier<HttpAction>> centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile), null, null);
        assertThat(centralLogoutAction.isPresent(), is(false));
        verify(client, never()).getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), anyString());
    }

    @Test
    public void nullRedirectUrlNullClientRedirectAction() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), anyString())).thenReturn(null);
        final Optional<Supplier<HttpAction>> centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile), null, null);
        assertThat(centralLogoutAction.isPresent(), is(false));
    }

    @Test
    public void nullRedirectUrlNonNullClientRedirectAction() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_1));
        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile), null, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_1));
    }

    @Test
    public void invalidRedirectUrl() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), eq(VALID_REDIRECT_URL))).thenReturn(null);
        when(client.getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), eq(INVALID_REDIRECT_URL))).thenReturn(null);
        when(client.getLogoutAction(any(AsyncWebContext.class), any(CommonProfile.class), eq(null))).thenReturn(RedirectAction.redirect(LOGOUT_URL_1));
        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile), INVALID_REDIRECT_URL, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_1));
    }

    // Break after first action found
    @Test
    public void firstProfileLeadsToResult() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(secondProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(firstProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_1));
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(secondProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_2));

        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile, secondProfile), null, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_1));
    }

    @Test
    public void firstProfileNoResultSecondProfileResult() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(secondProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(firstProfile), anyString())).thenReturn(null);
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(secondProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_2));

        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile, secondProfile), null, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_2));
    }

    @Test
    public void firstProfileHasNoClientName() {
        when(firstProfile.getClientName()).thenReturn(null);
        when(secondProfile.getClientName()).thenReturn("client1");
        when(clients.findClient(eq("client1"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(firstProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_1));
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(secondProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_2));

        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile, secondProfile), null, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_2));
    }

    @Test
    public void firstProfileHasNoClient() {
        when(firstProfile.getClientName()).thenReturn("client1");
        when(secondProfile.getClientName()).thenReturn("client2");
        when(clients.findClient(eq("client1"))).thenReturn(null);
        when(clients.findClient(eq("client2"))).thenReturn(client);
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(firstProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_1));
        when(client.getLogoutAction(any(AsyncWebContext.class), eq(secondProfile), anyString())).thenReturn(RedirectAction.redirect(LOGOUT_URL_2));

        final HttpAction centralLogoutAction = CENTRAL_LOGOUT.getCentralLogoutAction(clients, Arrays.asList(firstProfile, secondProfile), null, webContext)
                .map(o -> o.get())
                .orElse(null);
        assertThat(centralLogoutAction, is(notNullValue()));
        assertThat(centralLogoutAction.getCode(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(status.get(), is(HttpConstants.TEMP_REDIRECT));
        assertThat(responseHeaders.get(HttpConstants.LOCATION_HEADER), is(LOGOUT_URL_2));

    }

}
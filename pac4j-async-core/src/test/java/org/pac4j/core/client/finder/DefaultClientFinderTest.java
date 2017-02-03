package org.pac4j.core.client.finder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncBaseClient;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.util.TestsConstants;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Adaptation of existing DefaultClientFInderTest to test the generic client finder approach
 */
public class DefaultClientFinderTest implements TestsConstants {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private final DefaultClientFinder finder = new DefaultClientFinder<AsyncBaseClient<?, ?>>();

    @Test
    public void testBlankClientName() {

        final List<AsyncClient> currentClients = finder.find(new Clients<AsyncBaseClient<?, ?>, AsyncAuthorizationGenerator>(),
                mockWebContext(), "  ");
        assertThat(currentClients, is(empty()));
    }

    @Test
    public void testClientOnRequestAllowed() {
        internalTestClientOnRequestAllowedList(NAME, NAME);
    }

    @Test
    public void testBadClientOnRequest() {
        final AsyncClient client = namedMockAsyncClient(NAME);
        final Clients clients = new Clients(client);
        final WebContext context = mockWebContextWithClientNameParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, FAKE_VALUE);
        expectedEx.expect(TechnicalException.class);
        expectedEx.expectMessage("No client found for name: " + FAKE_VALUE);
        finder.find(clients, context, NAME);
    }

    @Test
    public void testClientOnRequestAllowedList() {
        internalTestClientOnRequestAllowedList(NAME, FAKE_VALUE + "," + NAME);
    }

    @Test
    public void testClientOnRequestAllowedListCaseTrim() {
        internalTestClientOnRequestAllowedList("NaMe  ", FAKE_VALUE.toUpperCase() + "  ,       nAmE");
    }

    @Test
    public void testClientOnRequestNotAllowed() {
        final AsyncClient client1 = namedMockAsyncClient(NAME);
        final AsyncClient client2 = namedMockAsyncClient(CLIENT_NAME);
        final Clients clients = new Clients(client1, client2);
        final WebContext context = mockWebContextWithClientNameParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        expectedEx.expect(TechnicalException.class);
        expectedEx.expectMessage("Client not allowed: " + NAME);
        finder.find(clients, context, CLIENT_NAME);
    }

    @Test
    public void testClientOnRequestNotAllowedList() {
        final AsyncClient client1 = namedMockAsyncClient(NAME);
        final AsyncClient client2 = namedMockAsyncClient(CLIENT_NAME);
        final Clients clients = new Clients(client1, client2);
        final WebContext context = mockWebContextWithClientNameParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        expectedEx.expect(TechnicalException.class);
        expectedEx.expectMessage("Client not allowed: " + NAME);
        finder.find(clients, context, CLIENT_NAME + "," + FAKE_VALUE);
    }

    @Test
    public void testNoClientOnRequest() {
        final AsyncClient client1 = namedMockAsyncClient(NAME);
        final AsyncClient client2 = namedMockAsyncClient(CLIENT_NAME);
        final Clients clients = new Clients(client1, client2);
        final WebContext context = mockWebContext();
        final List<AsyncClient> currentClients = finder.find(clients, context, CLIENT_NAME);
        assertThat(currentClients.size(), is(1));
        assertThat(currentClients.get(0), is(client2));
    }

    @Test
    public void testNoClientOnRequestBadDefaultClient() {
        final AsyncClient client1 = namedMockAsyncClient(NAME);
        final AsyncClient client2 = namedMockAsyncClient(CLIENT_NAME);
        final Clients clients = new Clients(client1, client2);
        final WebContext context = mockWebContext();
        expectedEx.expect(TechnicalException.class);
        expectedEx.expectMessage("No client found for name: " + FAKE_VALUE);
        finder.find(clients, context, FAKE_VALUE);
    }

    @Test
    public void testNoClientOnRequestList() {
        internalTestNoClientOnRequestList(CLIENT_NAME + "," + NAME);
    }

    @Test
    public void testNoClientOnRequestListBlankSpaces() {
        internalTestNoClientOnRequestList(CLIENT_NAME + " ," + NAME);
    }

    @Test
    public void testNoClientOnRequestListDifferentCase() {
        internalTestNoClientOnRequestList(CLIENT_NAME.toUpperCase() + "," + NAME);
    }

    @Test
    public void testNoClientOnRequestListUppercase() {
        internalTestNoClientOnRequestList(CLIENT_NAME.toUpperCase() + "," + NAME);
    }


    private void internalTestClientOnRequestAllowedList(final String parameterName, final String names) {
        final AsyncClient client = namedMockAsyncClient(NAME);
        final Clients clients = new Clients(client);
        final WebContext context = mockWebContextWithClientNameParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, parameterName);
        final List<AsyncClient> currentClients = finder.find(clients, context, names);
        assertThat(currentClients.size(), is(1));
        assertThat(currentClients.get(0), is(client));
    }

    private void internalTestNoClientOnRequestList(final String names) {
        final AsyncClient client1 = namedMockAsyncClient(NAME);
        final AsyncClient client2 = namedMockAsyncClient(CLIENT_NAME);
        final Clients clients = new Clients(client1, client2);
        final WebContext context = mockWebContext();
        final List<AsyncClient> currentClients = finder.find(clients, context, names);
        assertThat(currentClients.size(), is(2));
        assertThat(currentClients.get(0), is(client2));
        assertThat(currentClients.get(01), is(client1));
    }

    private final AsyncClient namedMockAsyncClient(final String name) {
        final AsyncClient client = mock(AsyncClient.class);
        when(client.getName()).thenReturn(name);
        return client;
    }

    private final WebContext mockWebContext() {
        final WebContext context = mock(WebContext.class);
        return context;
    }

    private final WebContext mockWebContextWithClientNameParameter(final String parameterName, final String parameterValue) {
        final WebContext context = mock(WebContext.class);
        when(context.getRequestParameter(parameterName)).thenReturn(parameterValue);
        return context;
    }

}
package org.pac4j.async.vertx.context;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.auth.Pac4jUser;
import org.pac4j.core.context.Cookie;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class VertxAsyncWebContext implements AsyncWebContext {

    final RoutingContext routingContext;
    final VertxAsynchronousComputationAdapter computationAdapter;
    private final String method;
    private final String serverName;
    private final int serverPort;
    private final String fullUrl;
    private final String scheme;
    private final String remoteAddress;
    private final JsonObject headers;
    private final JsonObject parameters;
    private final Map<String, String[]> mapParameters;
    private final AsyncSessionStore sessionStore;

    private boolean contentHasBeenWritten = false; // Need to set chunked before first write of any content

    public VertxAsyncWebContext(final RoutingContext routingContext,
                                final VertxAsynchronousComputationAdapter computationAdapter,
                                final AsyncSessionStore asyncSessionStore) {
        Objects.requireNonNull(routingContext);
        Objects.requireNonNull(computationAdapter);
        this.routingContext = routingContext;
        this.computationAdapter = computationAdapter;
        this.sessionStore = asyncSessionStore;

        final HttpServerRequest request = routingContext.request();
        this.method = request.method().toString();
        this.fullUrl = request.absoluteURI();
        URI uri;
        try {
            uri = new URI(fullUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Request to invalid URL " + fullUrl + " while constructing VertxWebContext");
        }
        this.scheme = uri.getScheme();
        this.serverName = uri.getHost();
        this.serverPort = (uri.getPort() != -1) ? uri.getPort() : scheme.equals("http") ? 80 : 443;
        this.remoteAddress = request.remoteAddress().toString();

        headers = new JsonObject();
        for (String name : request.headers().names()) {
            headers.put(name, request.headers().get(name));
        }

        parameters = new JsonObject();
        for (String name : request.params().names()) {
            parameters.put(name, new JsonArray(Arrays.asList(request.params().getAll(name).toArray())));
        }

        mapParameters = new HashMap<>();
        for (String name : parameters.fieldNames()) {
            JsonArray params = parameters.getJsonArray(name);
            String[] values = new String[params.size()];
            int i = 0;
            for (Object o : params) {
                values[i++] = (String) o;
            }
            mapParameters.put(name, values);
        }
    }

    @Override
    public AsyncPac4jExecutionContext getExecutionContext() {
        return computationAdapter.getExecutionContext();
    }

    @Override
    public AsynchronousComputationAdapter getAsyncComputationAdapter() {
        return computationAdapter;
    }

    public void failResponse(final int status) {
        routingContext.fail(status);
    }

    public void completeResponse() {
        routingContext.response().end();
    }

    @Override
    public String getRequestParameter(String name) {
        JsonArray values = parameters.getJsonArray(name);
        if (values != null && values.size() > 0) {
            return values.getString(0);
        }
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return mapParameters;
    }

    @Override
    public Object getRequestAttribute(String s) {
        return routingContext.get(s);
    }

    @Override
    public void setRequestAttribute(String s, Object o) {
        routingContext.put(s, o);
    }

    @Override
    public String getRequestHeader(String name) {
        return headers.getString(name);
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    @Override
    public void writeResponseContent(String content) {
        if (content != null && !content.isEmpty()) {
            if (!contentHasBeenWritten) {
                routingContext.response().setChunked(true);
                contentHasBeenWritten = true;
            }
            routingContext.response().write(content);
        }
    }

    @Override
    public void setResponseStatus(int code) {
        routingContext.response().setStatusCode(code);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        routingContext.response().putHeader(name, value);
    }

    public Map<String, String> getResponseHeaders() {
        return routingContext.response().headers().entries().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    @Override
    public void setResponseContentType(String s) {
        routingContext.response().headers().add("Content-Type", s);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public boolean isSecure() {
        return getScheme().equals("https");
    }

    @Override
    public String getFullRequestURL() {
        return fullUrl;
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        return routingContext.cookies().stream().map(cookie -> {
            final Cookie p4jCookie = new Cookie(cookie.getName(), cookie.getValue());
            p4jCookie.setDomain(cookie.getDomain());
            p4jCookie.setPath(cookie.getPath());
            return p4jCookie;
        }).collect(Collectors.toList());
    }

    @Override
    public void addResponseCookie(Cookie cookie) {
        routingContext.addCookie(io.vertx.ext.web.Cookie.cookie(cookie.getName(), cookie.getValue()));
    }

    @Override
    public String getPath() {
        return routingContext.request().path();
    }

    @Override
    public <T extends AsyncSessionStore> T getSessionStore() {
        return (T) sessionStore;
    }

    @Override
    public <T extends AsyncSessionStore> void setSessionStore(T sessionStore) {
        throw new UnsupportedOperationException("Cannot set session store on VertxAsyncWebContext");
    }

    public Pac4jUser getVertxUser() {
        return (Pac4jUser) routingContext.user();
    }

    public void removeVertxUser() {
        routingContext.clearUser();
    }

    public void setVertxUser(final Pac4jUser pac4jUser) {
        routingContext.setUser(pac4jUser);
    }

    public Session getVertxSession() {
        return routingContext.session();
    }

}

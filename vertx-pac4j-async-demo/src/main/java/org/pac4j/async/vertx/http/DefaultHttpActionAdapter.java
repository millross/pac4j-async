package org.pac4j.async.vertx.http;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;

import java.util.Optional;

/**
 *
 */
public class DefaultHttpActionAdapter implements HttpActionAdapter<Void, VertxAsyncWebContext> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpActionAdapter.class);

    @Override
    public Void adapt(int code, VertxAsyncWebContext context) {
        if (code == HttpConstants.UNAUTHORIZED) {
            sendFailureResponse(context, HttpConstants.UNAUTHORIZED);
        } else if (code == HttpConstants.FORBIDDEN) {
            sendFailureResponse(context, HttpConstants.FORBIDDEN);
        } else if (code == HttpConstants.TEMP_REDIRECT) {
            final Optional<String> location = getLocation(context);
            // This is clunkier than it should be due to Java 8 Optional limitation
            location.orElseThrow(() -> new TechnicalException("Redirect without a location header"));
            location.ifPresent(l -> redirect(l, context));
        } else if (code == HttpConstants.OK) {
            // Content should already have been written
            context.setResponseStatus(HttpConstants.OK);
            context.setResponseHeader(HttpConstants.CONTENT_TYPE_HEADER, HttpConstants.HTML_CONTENT_TYPE);
            context.completeResponse();
        } else {
            final String message = "Unsupported HTTP action: " + code;
            LOG.error(message);
            throw new TechnicalException(message);
        }
        return null;
    }

    private Optional<String> getLocation(final VertxAsyncWebContext webContext) {
        return Optional.ofNullable(webContext.getResponseHeaders().get(HttpConstants.LOCATION_HEADER));

    }

    protected void redirect(final String location, final VertxAsyncWebContext webContext) {
        webContext.setResponseStatus(HttpConstants.TEMP_REDIRECT);
        webContext.setResponseHeader(HttpConstants.LOCATION_HEADER, location);
        webContext.completeResponse();
    }

    protected void sendFailureResponse(final VertxAsyncWebContext webContext, final int code) {
        webContext.failResponse(code);
    }


}

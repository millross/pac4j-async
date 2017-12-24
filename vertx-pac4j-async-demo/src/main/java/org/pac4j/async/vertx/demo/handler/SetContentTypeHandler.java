package org.pac4j.async.vertx.demo.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 *
 */
public class SetContentTypeHandler implements Handler<RoutingContext> {

    private final CharSequence contentType;

    public SetContentTypeHandler(CharSequence contentType) {
        this.contentType = contentType;
    }

    @Override
    public void handle(RoutingContext rc) {
        rc.response().putHeader(CONTENT_TYPE, contentType);
        rc.next();
    }
}

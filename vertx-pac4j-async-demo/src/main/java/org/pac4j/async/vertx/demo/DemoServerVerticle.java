package org.pac4j.async.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

/**
 *
 */
public class DemoServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServerVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);

        // Only use the following handlers where we want to use sessions - this is enforced by the regexp
        router.get("/").handler(rc -> {
            rc.response().setStatusCode(200);
            rc.response().end("Hello, world");
        });
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);

    }

}

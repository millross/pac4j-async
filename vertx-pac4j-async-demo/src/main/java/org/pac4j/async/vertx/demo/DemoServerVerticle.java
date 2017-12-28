package org.pac4j.async.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.demo.handler.IndexHandler;
import org.pac4j.async.vertx.demo.handler.SetContentTypeHandler;

/**
 *
 */
public class DemoServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServerVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        final Context context = vertx.getOrCreateContext();
        final VertxAsynchronousComputationAdapter computationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);

        router.get("/").handler(new SetContentTypeHandler(HttpHeaders.TEXT_HTML));
        router.get("/").handler(new IndexHandler(computationAdapter));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);

    }

}

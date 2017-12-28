package org.pac4j.async.vertx.demo.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.async.vertx.demo.DemoFunctions;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Public index handler for the vertx pac4j async demo
 */
public class IndexHandler implements Handler<RoutingContext> {

    private static final HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();

    private final VertxAsynchronousComputationAdapter computationAdapter;

    public IndexHandler(VertxAsynchronousComputationAdapter computationAdapter) {
        this.computationAdapter = computationAdapter;
    }

    private CompletableFuture<List<CommonProfile>> getUserProfiles(final RoutingContext rc) {
        final AsyncProfileManager<CommonProfile, VertxAsyncWebContext> profileManager = DemoFunctions.getProfileManager(rc, this.computationAdapter);
        return profileManager.getAll(true);
    }


    @Override
    public void handle(RoutingContext rc) {
        // we define a hardcoded title for our application
        rc.put("name", "Vert.x Web");

        getUserProfiles(rc)
            .thenAccept(profiles -> rc.put("userProfiles", profiles))
            .thenAccept(v -> {
                // and now delegate to the engine to render it.
                engine.render(rc, "templates/index.hbs", res -> {
                    if (res.succeeded()) {
                        rc.response().end(res.result());
                    } else {
                        rc.fail(res.cause());
                    }
                });
            });

    }

}

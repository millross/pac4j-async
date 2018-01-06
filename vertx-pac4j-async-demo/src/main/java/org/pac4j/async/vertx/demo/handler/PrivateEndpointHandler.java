package org.pac4j.async.vertx.demo.handler;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.async.vertx.VertxAsyncProfileManager;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 *
 */
public class PrivateEndpointHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext rc) {

    }

    public static Handler<RoutingContext> generateProtectedIndex(final BiConsumer<RoutingContext, Buffer> generatedContentConsumer,
                                                                 final VertxAsynchronousComputationAdapter asynchronousComputationAdapter) {

        final HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();

        return rc -> {
            // and now delegate to the engine to render it.

            final CompletableFuture<List<CommonProfile>> profileFuture = getUserProfiles(rc, asynchronousComputationAdapter);
            profileFuture.thenAccept(profile -> {
                rc.put("userProfiles", profile);

                engine.render(rc, "", "templates/protectedIndex.hbs", res -> {
                    if (res.succeeded()) {
                        generatedContentConsumer.accept(rc, res.result());
                    } else {
                        rc.fail(res.cause());
                    }
                });
            });
        };
    }

    private static CompletableFuture<List<CommonProfile>> getUserProfiles(final RoutingContext rc, final VertxAsynchronousComputationAdapter asynchronousComputationAdapter) {
        final AsyncProfileManager<CommonProfile, VertxAsyncWebContext> profileManager = new VertxAsyncProfileManager(new VertxAsyncWebContext(rc, asynchronousComputationAdapter));
        return profileManager.getAll(true);
    }


}

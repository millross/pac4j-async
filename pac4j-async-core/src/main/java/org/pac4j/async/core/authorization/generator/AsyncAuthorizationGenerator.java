package org.pac4j.async.core.authorization.generator;

import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Asynchronous version of pac4j's synchronous AuthorizationGenerator class, to be used for implementing non-blocking
 * auth generator implementations, as well as for wrapping synchronous existing auth generators in async code for use
 * in async fraameworks.
 */
public interface AsyncAuthorizationGenerator <U extends CommonProfile> {

    Logger LOG = LoggerFactory.getLogger(AsyncAuthorizationGenerator.class);
    /**
     * Given an existing non-blocking but synchronous AuthorizationGenerator, convert it into an async non-blocking
     * one
     */
    static <T extends CommonProfile> AsyncAuthorizationGenerator<T> fromNonBlockingAuthorizationGenerator(AuthorizationGenerator<WebContextBase<?>, T> authGen) {
        return (context, profile) -> AsynchronousComputationAdapter.fromNonBlocking(
                () -> {
                    if (profile != null) {
                        authGen.generate(context, profile);
                    } else {
                        LOG.warn("Unexpected null profile in non-blocking generator derived from {}", authGen);
                    }
                    return t -> { };
                }
        );
    }

    /**
     * Given an existing blocking synchronous AuthorizationGenerator, convert it into an async
     * one
     */
    static <T extends CommonProfile> AsyncAuthorizationGenerator<T> fromBlockingAuthorizationGenerator(AuthorizationGenerator<WebContextBase<?>, T> authGen,
                                                                                                       AsynchronousComputationAdapter asyncComputation) {
        return (context, profile) -> asyncComputation.fromBlocking(
                () -> {
                    // several of these could run in parallel so we need to synchronize the profile as we don't know
                    // what the generator might do to its state so we need to ensure that only one thread gets to write
                    // its state at a time.
                    if (profile != null) {
                        synchronized (profile) {
                            authGen.generate(context, profile);
                        }
                    } else {
                        LOG.warn("Unexpected null profile in blocking generator derived from {}", authGen);
                    }

                    return t -> { };
                }
        );
    }

    /**
     * Generate the authorization information from and for the user profile.
     *
     * @param profile the user profile for which to generate the authorization information.
     */
    CompletableFuture<Consumer<U>> generate(AsyncWebContext context, U profile);

}

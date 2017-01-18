package org.pac4j.async.core.authorization.generator;

import org.pac4j.async.core.AsynchronousComputation;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of pac4j's synchronous AuthorizationGenerator class, to be used for implementing non-blocking
 * auth generator implementations, as well as for wrapping synchronous existing auth generators in async code for use
 * in async fraameworks.
 */
public interface AsyncAuthorizationGenerator <U extends CommonProfile> {

    /**
     * Given an existing non-blocking but synchronous AuthorizationGenerator, convert it into an async non-blocking
     * one
     */
    static <T extends CommonProfile> AsyncAuthorizationGenerator<T> fromNonBlockingAuthorizationGenerator(AuthorizationGenerator<T> authGen) {
        return profile -> AsynchronousComputation.fromNonBlocking(
                () -> {
                    authGen.generate(profile);
                    return null;
                }
        );
    }

    /**
     * Given an existing blocking synchronous AuthorizationGenerator, convert it into an async
     * one
     */
    static <T extends CommonProfile> AsyncAuthorizationGenerator<T> fromBlockingAuthorizationGenerator(AuthorizationGenerator<T> authGen,
                                                                                                       AsynchronousComputation asyncComputation) {
        return profile -> asyncComputation.fromBlocking(
                () -> {
                    // several of these could run in parallel so we need to synchronize the profile as we don't know
                    // what the generator might do to its state so we need to ensure that only one thread gets to write
                    // its state at a time.
                    synchronized (profile) {
                        authGen.generate(profile);
                    }
                    return null;
                }
        );
    }

    /**
     * Generate the authorization information from and for the user profile.
     *
     * @param profile the user profile for which to generate the authorization information.
     */
    CompletableFuture<Void> generate(U profile);

}

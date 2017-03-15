package org.pac4j.async.core.authorization.authorizer;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.AsynchronousComputation;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Async version of a pac4j authorizer - we need to convert sync versions into the async version for use. This is
 * merely a simple type definition to keep everything on site.
 *
 */
public interface AsyncAuthorizer<U extends CommonProfile> {

    Logger LOG = LoggerFactory.getLogger(AsyncAuthorizer.class);

    /**
     * Given an existing non-blocking but synchronous Authorizater, convert it into an async non-blocking
     * one
     */
    static <U extends CommonProfile> AsyncAuthorizer<U> fromNonBlockingAuthorizer(final Authorizer<WebContextBase<?>, U> syncAuthorizer) {
        return (context, profiles) -> AsynchronousComputation.fromNonBlocking(ExceptionSoftener.softenSupplier(() -> syncAuthorizer.isAuthorized(context, profiles)));
    }

    CompletableFuture<Boolean> isAuthorized(AsyncWebContext<?> context, List<U> profiles);

}

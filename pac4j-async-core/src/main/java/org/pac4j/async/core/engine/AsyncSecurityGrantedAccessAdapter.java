package org.pac4j.async.core.engine;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface AsyncSecurityGrantedAccessAdapter<R, C extends AsyncWebContext> {
    /**
     * Adapt the current successful action as the expected result.
     *
     * @param context the web context
     * @param parameters additional parameters
     * @return a completable future which will complete with the adapted result
     */
    CompletableFuture<R> adapt(C context, Object... parameters) ;

}

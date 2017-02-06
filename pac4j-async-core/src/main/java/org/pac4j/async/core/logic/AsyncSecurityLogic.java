package org.pac4j.async.core.logic;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface AsyncSecurityLogic<R, C extends WebContext> {

    /**
     * Perform the security logic, assuming that our clients may be async and off-thread, therefore we accept a consumer
     * callback to handle the result of this computation.
     *
     * Note that we expect the config, http action adapter, client names, authorizers, matchers and muiltiprofile to
     * be supplied as part of the config and not to change from call to call. This is the normal behaviour when used
     * in both vertx-pac4j and play-pac4j. If someone wants to override that behaviour, they can add those parameters
     * to a non-standard call. I also reserve the right to add them to the signature again later if nevessary. 
     *
     * @param context the web context
     * @param securityGrantedAccessAdapter the success adapter
     * @param parameters additional parameters
     */
    CompletableFuture<R> perform(C context, SecurityGrantedAccessAdapter<R, C> securityGrantedAccessAdapter,
                                 Object... parameters);


}

package org.pac4j.async.core.logic;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of the sync LogoutLogic class for use by async frameworks and the async pac4j API
 */
public interface AsyncLogoutLogic<R, C extends AsyncWebContext> {

    /**
     * Perform the application logout logic.
     *
     * @param context the web context
     * @return the resulting action for logout
     */
    CompletableFuture<R> perform(C context);

}

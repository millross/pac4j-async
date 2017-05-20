package org.pac4j.async.core.logic;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.HttpActionAdapter;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of existing sync CallbackLogic
 */
public interface AsyncCallbackLogic<R, C extends AsyncWebContext> {
    CompletableFuture<R> perform(C context, Config config, HttpActionAdapter<R, C> httpActionAdapter,
                                 String defaultUrl, Boolean multiProfile, Boolean renewSession);
}

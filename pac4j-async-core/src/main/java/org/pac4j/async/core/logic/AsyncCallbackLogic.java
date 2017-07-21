package org.pac4j.async.core.logic;

import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of existing sync CallbackLogic
 */
public interface AsyncCallbackLogic<R, U extends CommonProfile, C extends AsyncWebContext> {
    CompletableFuture<R> perform(C context, AsyncConfig<R, U, C> config, HttpActionAdapter<R, C> httpActionAdapter,
                                 String defaultUrl, Boolean renewSession);
}

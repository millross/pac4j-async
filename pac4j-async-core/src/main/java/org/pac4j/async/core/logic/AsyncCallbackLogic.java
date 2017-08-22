package org.pac4j.async.core.logic;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of existing sync CallbackLogic
 */
public interface AsyncCallbackLogic<R, U extends CommonProfile, C extends AsyncWebContext> {
    CompletableFuture<R> perform(C context, String defaultUrls);
}

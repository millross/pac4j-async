package org.pac4j.async.core.redirect;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.redirect.RedirectAction;

import java.util.concurrent.CompletableFuture;

/**
 * Return a redirection to perfom - async version where we need to do async i/o (e.g. side effects reading from/writing to session).
 *
 * @author Jerome Leleu
 * @since 1.9.0
 */
public interface AsyncRedirectActionBuilder {
    /**
     * Return a CompletableFuture which will complete with a redirect action for the web context.
     * If a specific HTTP action is required, then the future will complete exceptionally with that HttpAction
     * and if other exceptions are experienced, then it will complete exceptionally
     *
     * @param context the web context
     * @return future which will complete with the redirect action
     */
    CompletableFuture<RedirectAction> redirect(AsyncWebContext context);

}

package org.pac4j.async.core.logic;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.HttpActionAdapter;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of the sync LogoutLogic class for use by async frameworks and the async pac4j API
 */
public interface AsyncLogoutLogic<R, C extends AsyncWebContext> {

    /**
     * Perform the application logout logic.
     *
     * @param context the web context
     * @param config the security configuration
     * @param httpActionAdapter the HTTP action adapter
     * @param defaultUrl the default url
     * @param logoutUrlPattern the logout url pattern
     * @param localLogout whether a local logout is required
     * @param destroySession whether the web session must be destroyed
     * @param centralLogout whether a central logout is required
     * @return the resulting action for logout
     */
    CompletableFuture<R> perform(C context, Config config, HttpActionAdapter<R, C> httpActionAdapter, String defaultUrl,
                                String logoutUrlPattern, Boolean localLogout, Boolean destroySession, Boolean centralLogout);

}

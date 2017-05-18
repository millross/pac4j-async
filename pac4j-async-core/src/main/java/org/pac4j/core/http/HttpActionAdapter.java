package org.pac4j.core.http;

import org.pac4j.core.context.WebContextBase;

/**
 * Async-usable http action adapter. Initially we won't define an async http action adapter, as this will use non-blocking
 * methods on webcontext only. If we find we might need to call async webcontext methods we'll introduce a fully async
 * version o this interface.
 */
public interface HttpActionAdapter <R, C extends WebContextBase<?>> {
    /**
     * Adapt the HTTP action.
     *
     * @param code the HTTP action status code
     * @param context the web context
     * @return the specific framework HTTP result
     */
    R adapt(int code, C context);
}

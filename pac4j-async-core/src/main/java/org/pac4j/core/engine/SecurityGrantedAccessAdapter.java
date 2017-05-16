package org.pac4j.core.engine;

import org.pac4j.core.context.WebContextBase;

/**
 *
 */
public interface SecurityGrantedAccessAdapter<R, C extends WebContextBase<?>> {

    /**
     * Adapt the current successful action as the expected result.
     *
     * @param context    the web context
     * @param parameters additional parameters
     * @return an adapted result
     * @throws Throwable any exception / error
     */
    R adapt(C context, Object... parameters) throws Throwable;
}
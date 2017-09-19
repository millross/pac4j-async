package org.pac4j.core.matching;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;

import java.util.Map;

/**
 * The way to check requests matching - adapted to permit conversion to async version where only non-blocking i/o is used.
 *
 * @author Jerome Leleu
 * @since 1.8.1
 */
public interface MatchingChecker<C extends WebContext<?>, M> {

    /**
     * Check if the web context matches.
     *
     * @param context the web context, extender of WebContext
     * @param matcherNames the matchers
     * @param matchersMap the map of matchers
     * @return whether the web context matches
     * @throws HttpAction whether an additional HTTP action is required
     */
    boolean matches(C context, String matcherNames, Map<String, M> matchersMap) throws HttpAction;
}
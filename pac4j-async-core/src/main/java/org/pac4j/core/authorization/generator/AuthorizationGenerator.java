package org.pac4j.core.authorization.generator;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.profile.CommonProfile;

/**
 * Potentially async-convertible version of authorization generator. Any sync generator which
 * does non-blocking i/o can be safely converted to an async generator via an automatic
 * conversion
 */
public interface AuthorizationGenerator<C extends WebContextBase<?>, U extends CommonProfile> {

    /**
     * Generate the authorization information from and for the user profile.
     *
     * @param context the web context
     * @param profile the user profile for which to generate the authorization information.
     * @return the updated profile or a new one
     */
    U generate(C context, U profile);
}

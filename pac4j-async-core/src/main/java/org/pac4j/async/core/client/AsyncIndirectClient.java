package org.pac4j.async.core.client;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

/**
 *
 */
public abstract class AsyncIndirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {
    public RedirectAction getRedirectAction(final AsyncWebContext context) throws HttpAction {
        return null;
    }
}

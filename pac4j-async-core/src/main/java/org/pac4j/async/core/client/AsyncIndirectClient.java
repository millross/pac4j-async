package org.pac4j.async.core.client;

import org.pac4j.async.core.context.ContextRunner;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public abstract class AsyncIndirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {
    public AsyncIndirectClient(ContextRunner contextRunner) {
        super(contextRunner);
    }
}

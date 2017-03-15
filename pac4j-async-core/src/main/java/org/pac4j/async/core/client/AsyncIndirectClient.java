package org.pac4j.async.core.client;

import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public abstract class AsyncIndirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {
    public AsyncIndirectClient(AsyncPac4jExecutionContext contextRunner) {
        super(contextRunner);
    }
}

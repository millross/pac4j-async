package org.pac4j.async.core.client;

import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

/**
 * Async version of the pacr4j sync direct client
 */
public abstract class AsyncDirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {
}

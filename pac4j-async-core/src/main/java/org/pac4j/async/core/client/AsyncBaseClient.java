package org.pac4j.async.core.client;

import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.UserProfile;

/**
 *
 */
public abstract class AsyncBaseClient<C extends Credentials, U extends UserProfile> implements AsyncClient<C, U> {
}

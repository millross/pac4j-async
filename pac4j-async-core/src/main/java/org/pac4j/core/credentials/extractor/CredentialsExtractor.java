package org.pac4j.core.credentials.extractor;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;

/**
 * An extractor gets the {@link Credentials} from a {@link WebContextBase} and should return <code>null</code> if no credentials are present
 * or should throw a {@link CredentialsException} if it cannot get it. Updated to enable easy use in async pac4j
 * implementations/APIs
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public interface CredentialsExtractor<C extends Credentials, W extends WebContextBase<?>> {

    /**
     * Extract the right credentials. It should throw a {@link CredentialsException} in case of failure.
     *
     * @param context the current web context
     * @return the credentials
     * @throws HttpAction           requires a specific HTTP action if necessary
     * @throws CredentialsException the credentials are invalid
     */
    C extract(W context) throws HttpAction, CredentialsException;
}


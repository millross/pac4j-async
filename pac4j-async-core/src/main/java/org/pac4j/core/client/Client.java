package org.pac4j.core.client;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

/**
 * <p>This interface is the core class of the library. It represents an authentication mechanism to validate user's credentials and
 * retrieve his user profile.</p>
 * <p>Clients can be "indirect": in that case, credentials are not provided with the HTTP request, but the user must be redirected to
 * an identity provider to perform login, the original requested url being saved and restored after the authentication process is done.</p>
 * <p>The {@link #redirect(WebContextBase)} method is called to redirect the user to the identity provider,
 * the {@link #getCredentials(WebContextBase)} method is used to retrieve the credentials provided by the remote identity provider and
 * the {@link #getUserProfile(Credentials, WebContextBase)} method is called to get the user profile from the identity provider and based
 * on the provided credentials.</p>
 * <p>Clients can be "direct": in that case, credentials are provided along with the HTTP request and validated by the application.</p>
 * <p>The {@link #redirect(WebContextBase)} method is not used, the {@link #getCredentials(WebContextBase)} method is used to retrieve
 * and validate the credentials provided and the {@link #getUserProfile(Credentials, WebContextBase)} method is called to get the user profile from
 * the appropriate system.</p>
 * 
 * @author Jerome Leleu
 * @since 1.4.0
 */
public interface Client<C extends Credentials, U extends CommonProfile, WC extends WebContextBase<?>> {

    /**
     * Get the name of the client.
     * 
     * @return the name of the client
     */
    String getName();

    /**
     * <p>Redirect to the authentication provider for an indirect client.</p>
     *
     * @param context the current web context
     * @return the performed redirection
     * @throws HttpAction whether an additional HTTP action is required
     */
    HttpAction redirect(WC context) throws HttpAction;

    /**
     * <p>Get the credentials from the web context. If no validation was made remotely (direct client), credentials must be validated at this step.</p>
     * <p>In some cases, a {@link HttpAction} may be thrown instead.</p>
     *
     * @param context the current web context
     * @return the credentials
     * @throws HttpAction whether an additional HTTP action is required
     */
    C getCredentials(WC context) throws HttpAction;

    /**
     * Get the user profile based on the provided credentials.
     * 
     * @param credentials credentials
     * @param context web context 
     * @return the user profile
     * @throws HttpAction whether an additional HTTP action is required
     */
    U getUserProfile(C credentials, WC context) throws HttpAction;

    /**
     * <p>Return the logout action (indirect clients).</p>
     *
     * @param context the current web context
     * @param currentProfile the currentProfile
     * @param targetUrl the target url after logout
     * @return the redirection
     */
    RedirectAction getLogoutAction(WC context, U currentProfile, String targetUrl);
}

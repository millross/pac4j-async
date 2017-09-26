package org.pac4j.core.client;

import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.core.util.CommonHelper;

/**
 *
 */
public abstract class DirectClient<C extends Credentials, U extends CommonProfile, WC extends WebContext<?>, AG extends AuthorizationGenerator<WC, U>>
        extends BaseClient<C, U , WC , AG> {

    @Override
    protected void internalInit(final WC context) {
        clientInit(context);

        // ensures components have been properly initialized
        CommonHelper.assertNotNull("credentialsExtractor", getCredentialsExtractor());
        CommonHelper.assertNotNull("authenticator", getAuthenticator());
        CommonHelper.assertNotNull("profileCreator", getProfileCreator());
    }

    /**
     * Initialize the client.
     *
     */
    protected abstract void clientInit(final WC context);

    @Override
    public final HttpAction redirect(final WC context) throws HttpAction {
        throw new TechnicalException("direct clients do not support redirections");
    }

    @Override
    public final C getCredentials(final WC context) throws HttpAction {
        init(context);
        return retrieveCredentials(context);
    }

    @Override
    public final RedirectAction getLogoutAction(final WC context, final U currentProfile, final String targetUrl) {
        return null;
    }

    @Override
    public boolean isIndirect() {
        return false;
    }
}

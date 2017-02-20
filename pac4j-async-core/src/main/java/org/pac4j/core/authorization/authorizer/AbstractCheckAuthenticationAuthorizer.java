package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public abstract class AbstractCheckAuthenticationAuthorizer<C extends WebContextBase<?>, U extends CommonProfile> extends ProfileAuthorizer<C, U>{
    private String redirectionUrl;

    public AbstractCheckAuthenticationAuthorizer() {}

    public AbstractCheckAuthenticationAuthorizer(final String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }

    @Override
    protected boolean handleError(final C context) throws HttpAction {
        if (this.redirectionUrl != null) {
            throw HttpAction.redirect(getErrorMessage(), context, this.redirectionUrl);
        } else {
            return false;
        }
    }

    /**
     * Return the error message.
     *
     * @return the error message.
     */
    protected abstract String getErrorMessage();

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }
}

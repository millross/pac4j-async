package org.pac4j.async.core.client;

import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuth;
import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuthenticationStrategy;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.core.redirect.RedirectActionBuilder;

/**
 *
 */
public abstract class AsyncIndirectClient<C extends Credentials, U extends CommonProfile> extends AsyncBaseClient<C, U> {

    private RedirectActionBuilder redirectActionBuilder;

    public RedirectAction getRedirectAction(final AsyncWebContext context) throws HttpAction {
        return null;
    }

    @Override
    protected final RecordFailedAuthenticationStrategy authFailureRecorder() {
        return RecordFailedAuth.RECORD_IN_SESSION;
    }

    public RedirectActionBuilder getRedirectActionBuilder() {
        return redirectActionBuilder;
    }

    public void setRedirectActionBuilder(RedirectActionBuilder redirectActionBuilder) {
        this.redirectActionBuilder = redirectActionBuilder;
    }

    protected void defaultRedirectActionBuilder(final RedirectActionBuilder redirectActionBuilder) {
        if (this.redirectActionBuilder == null) {
            this.redirectActionBuilder = redirectActionBuilder;
        }
    }
}

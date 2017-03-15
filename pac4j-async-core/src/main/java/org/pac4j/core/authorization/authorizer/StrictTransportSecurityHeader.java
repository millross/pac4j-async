package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.ContextHelper;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class StrictTransportSecurityHeader implements Authorizer<WebContextBase<?>, CommonProfile> {

    /*
         * 6 months in seconds.
     */
    private final static int DEFAULT_MAX_AGE = 15768000;

    private int maxAge = DEFAULT_MAX_AGE;

    public StrictTransportSecurityHeader() {}

    public StrictTransportSecurityHeader(final int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public Boolean isAuthorized(final WebContextBase<?> context, final List<CommonProfile> profiles) throws HttpAction {
        if (ContextHelper.isHttpsOrSecure(context)) {
            context.setResponseHeader("Strict-Transport-Security", "max-age=" + maxAge + " ; includeSubDomains");
        }
        return true;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }


}

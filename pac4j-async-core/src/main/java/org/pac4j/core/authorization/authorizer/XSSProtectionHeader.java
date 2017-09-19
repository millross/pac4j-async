package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class XSSProtectionHeader implements Authorizer<WebContext, CommonProfile> {
    @Override
    public Boolean isAuthorized(WebContext context, List<CommonProfile> profiles) throws HttpAction {
        context.setResponseHeader("X-XSS-Protection", "1; mode=block");
        return true;

    }
}

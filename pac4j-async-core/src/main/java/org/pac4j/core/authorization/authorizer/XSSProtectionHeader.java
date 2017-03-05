package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class XSSProtectionHeader implements Authorizer<WebContextBase, CommonProfile> {
    @Override
    public Boolean isAuthorized(WebContextBase context, List<CommonProfile> profiles) throws HttpAction {
        context.setResponseHeader("X-XSS-Protection", "1; mode=block");
        return true;

    }
}

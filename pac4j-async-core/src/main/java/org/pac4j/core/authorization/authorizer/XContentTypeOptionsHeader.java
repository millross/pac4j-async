package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 * XContent type options header.
 *
 * @author Jerome Leleu
 * @since 1.8.1
 */
public class XContentTypeOptionsHeader implements Authorizer<WebContext, CommonProfile> {
    @Override
    public Boolean isAuthorized(WebContext context, List<CommonProfile> profiles) throws HttpAction {
        context.setResponseHeader("X-Content-Type-Options", "nosniff");
        return true;
    }
}

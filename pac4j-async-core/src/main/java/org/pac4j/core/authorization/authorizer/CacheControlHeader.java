package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class CacheControlHeader implements Authorizer<WebContextBase, CommonProfile> {
    @Override
    public Boolean isAuthorized(WebContextBase context, List<CommonProfile> profiles) throws HttpAction {
        final String url = context.getFullRequestURL().toLowerCase();
        if (!url.endsWith(".css")
                && !url.endsWith(".js")
                && !url.endsWith(".png")
                && !url.endsWith(".jpg")
                && !url.endsWith(".ico")
                && !url.endsWith(".jpeg")
                && !url.endsWith(".bmp")
                && !url.endsWith(".gif")) {
            context.setResponseHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            context.setResponseHeader("Pragma", "no-cache");
            context.setResponseHeader("Expires", "0");
        }
        return true;
    }
}

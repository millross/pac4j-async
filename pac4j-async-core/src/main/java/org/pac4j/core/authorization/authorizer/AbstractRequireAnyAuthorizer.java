package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public abstract class AbstractRequireAnyAuthorizer<C extends WebContextBase<?>,E , U extends CommonProfile>
        extends AbstractRequireElementAuthorizer<C, E, U> {

    @Override
    protected Boolean isProfileAuthorized(final C context, final U profile) throws HttpAction {
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        for (final E element : elements) {
            if (check(context, profile, element)) {
                return true;
            }
        }
        return false;
    }

}

package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class RequireAnyRoleAuthorizer <C extends WebContextBase<?>, U extends CommonProfile> extends AbstractRequireAnyAuthorizer<C, String, U>{

    public RequireAnyRoleAuthorizer() { }

    public RequireAnyRoleAuthorizer(final String... roles) {
        setElements(roles);
    }

    public RequireAnyRoleAuthorizer(final List<String> roles) {
        setElements(roles);
    }

    public RequireAnyRoleAuthorizer(final Set<String> roles) { setElements(roles); }

    @Override
    protected Boolean check(C context, U profile, String element) throws HttpAction {
        final Set<String> profileRoles = profile.getRoles();
        return profileRoles.contains(element);
    }
}

package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 * Generalised base authorizer which can represent either a sync or an async authorizer. We can auto-convert certain
 * sync implementors of these to async versions very easily, and this type abstraction makes this very possible
 */
public interface AuthorizerBase<C extends WebContextBase, R, P extends CommonProfile> {
    R isAuthorized(C context, List<P> profiles) throws HttpAction;
}

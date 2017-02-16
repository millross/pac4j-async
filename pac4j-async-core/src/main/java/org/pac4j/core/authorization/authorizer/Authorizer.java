package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.profile.CommonProfile;

/**
 * Sync authorizer meaning an individual sync authorizer can be tied to exactly what data it needs to pull from the
 * WebContext. If C is WebContextBase then we can convert it easily to an async authorizer, however if it is dependent
 * on elements of WebContext which could involve blocking i/o then we will not permit its conversion.
 */
public interface Authorizer<C extends WebContextBase, U extends  CommonProfile> extends AuthorizerBase<C, Boolean, U> {
}

package org.pac4j.async.core.config;

import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.logic.AsyncCallbackLogic;
import org.pac4j.async.core.logic.AsyncLogoutLogic;
import org.pac4j.async.core.logic.AsyncSecurityLogic;
import org.pac4j.async.core.matching.AsyncMatcher;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.config.Config;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public class AsyncConfig<R, U extends CommonProfile, C extends AsyncWebContext> extends Config<AsyncClient<? extends Credentials, ? extends U>, C, AsyncAuthorizer<CommonProfile>, AsyncMatcher, AsyncSecurityLogic<R, C>, AsyncCallbackLogic<R, U, C>, AsyncLogoutLogic<R, C>, AsyncSessionStore, AsyncProfileManager<U, C>, U> {
}

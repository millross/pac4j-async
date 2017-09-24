package org.pac4j.async.oauth.redirect;

import com.github.scribejava.core.oauth.OAuth20Service;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.redirect.AsyncRedirectActionBuilder;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.InitializableWebObject;
import org.pac4j.oauth.redirect.OAuth20RedirectActionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of OAuth 2.0 redirect action builder.
 *
 */
public class AsyncOauth20RedirectActionBuilder extends InitializableWebObject<WebContext<?>> implements AsyncRedirectActionBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OAuth20RedirectActionBuilder.class);

    protected final OAuth20Configuration configuration;

    public AsyncOauth20RedirectActionBuilder(OAuth20Configuration configuration) {
        CommonHelper.assertNotNull("configuration", configuration);
        this.configuration = configuration;
    }


    @Override
    public CompletableFuture<RedirectAction> redirect(AsyncWebContext context) {
        init(context);

        final CompletableFuture<OAuth20Service> serviceFuture;

        if (this.configuration.isWithState()) {
            final String state = getStateParameter();
            logger.debug("save sessionState: {}", state);
            serviceFuture = context.getSessionStore().set(context, this.configuration.getStateSessionAttributeName(), state)
                .thenApply(v -> this.configuration.buildService(context, state));
        } else {
            serviceFuture = CompletableFuture.completedFuture(this.configuration.getService());
        }

        return serviceFuture.thenApply(service -> service.getAuthorizationUrl(this.configuration.getCustomParams()))
                .thenApply(authUrl -> {
                    logger.debug("authorizationUrl: {}", authUrl);
                    return RedirectAction.redirect(authUrl);
                });


    }

    protected String getStateParameter() {
        final String stateData = this.configuration.getStateData();
        final String stateParameter;
        if (CommonHelper.isNotBlank(stateData)) {
            stateParameter = stateData;
        } else {
            stateParameter = CommonHelper.randomString(10);
        }
        return stateParameter;
    }


    @Override
    protected void internalInit(WebContext<?> context) {
        configuration.init(context);
    }
}

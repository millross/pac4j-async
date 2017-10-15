package org.pac4j.async.oauth.credentials.extractor;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.github.scribejava.core.utils.OAuthEncoder;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.oauth.config.OAuth20Configuration;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.exception.OAuthCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AsyncOAuth20CredentialsExtractor extends AsyncOAuthCredentialsExtractor<OAuth20Credentials, OAuth20Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncOAuth20CredentialsExtractor.class);

    public AsyncOAuth20CredentialsExtractor(OAuth20Configuration configuration) {
        super(configuration);
    }

    @Override
    protected CompletableFuture<OAuth20Credentials> getOAuthCredentials(AsyncWebContext context) {

        CompletableFuture<Void> stateChecksFuture = CompletableFuture.completedFuture(null); // Default to succeeded

        if (configuration.isWithState()) {

            final String stateParameter = context.getRequestParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER);

            if (CommonHelper.isNotBlank(stateParameter)) {
                final String stateSessionAttributeName = this.configuration.getStateSessionAttributeName();
                stateChecksFuture = context.getSessionStore().get(context, stateSessionAttributeName)
                        .thenCompose(sessionState -> context.getSessionStore().set(context, stateSessionAttributeName, null)
                                .thenAccept(v -> LOG.debug("sessionState: {} / stateParameter: {}", sessionState, stateParameter))
                                .thenApply(v -> (String) sessionState))
                                .thenAccept(ExceptionSoftener.softenConsumer(sessionState -> {
                                    if (!stateParameter.equals(sessionState)) {
                                        final String message = "State parameter mismatch: session expired or possible threat of cross-site request forgery";
                                        throw new OAuthCredentialsException(message);
                                    }
                                }));
            } else {
                stateChecksFuture = new CompletableFuture<>();
                final String message = "Missing state parameter: session expired or possible threat of cross-site request forgery";
                stateChecksFuture.completeExceptionally(new OAuthCredentialsException(message));
            }
        }

        return stateChecksFuture.thenApply(ExceptionSoftener.softenFunction(v -> {
            final String codeParameter = context.getRequestParameter(OAuth20Configuration.OAUTH_CODE);
            if (codeParameter != null) {
                final String code = OAuthEncoder.decode(codeParameter);
                logger.debug("code: {}", code);
                return new OAuth20Credentials(code, this.configuration.getClientName());
            } else {
                final String message = "No credential found";
                throw new OAuthCredentialsException(message);
            }
        }));

    }
}

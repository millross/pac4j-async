package org.pac4j.async.oauth.credentials.extractor;

import com.github.scribejava.core.exceptions.OAuthException;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.credentials.extractor.AsyncCredentialsExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.InitializableWebObject;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.exception.OAuthCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public abstract class AsyncOAuthCredentialsExtractor <C extends OAuthCredentials, O extends OAuthConfiguration>
        extends InitializableWebObject<AsyncWebContext> implements AsyncCredentialsExtractor<C> {

    protected final static Logger logger = LoggerFactory.getLogger(AsyncOAuthCredentialsExtractor.class);

    protected final O configuration;

    protected AsyncOAuthCredentialsExtractor(O configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void internalInit(final AsyncWebContext context) {
        CommonHelper.assertNotNull("configuration", this.configuration);
        configuration.init(context);
    }

    @Override
    public CompletableFuture<C> extract(AsyncWebContext context) {
        init(context);

        final boolean hasBeenCancelled = (Boolean) configuration.getHasBeenCancelledFactory().apply(context);
        // check if the authentication has been cancelled
        if (hasBeenCancelled) {
            logger.debug("authentication has been cancelled by user");
            return CompletableFuture.completedFuture(null);
        }

        // check errors
        try {
            boolean errorFound = false;
            final OAuthCredentialsException oauthCredentialsException = new OAuthCredentialsException("Failed to retrieve OAuth credentials, error parameters found");
            for (final String key : OAuthCredentialsException.ERROR_NAMES) {
                final String value = context.getRequestParameter(key);
                if (value != null) {
                    errorFound = true;
                    oauthCredentialsException.setErrorMessage(key, value);
                }
            }

            if (errorFound) {
                final CompletableFuture<C> errorFuture = new CompletableFuture<>();
                errorFuture.completeExceptionally(oauthCredentialsException);
                return errorFuture;
            } else {
                return getOAuthCredentials(context);
            }
        } catch (final OAuthException e) {
            final CompletableFuture<C> errorFuture = new CompletableFuture<>();
            errorFuture.completeExceptionally(new TechnicalException(e));
            return errorFuture;
        }

    }

    /**
     * Get the OAuth credentials from the web context.
     *
     * @param context the web context
     * @return the OAuth credentials
     * @throws HttpAction whether an additional HTTP action is required
     * @throws CredentialsException the credentials are invalid
     */
    protected abstract CompletableFuture<C> getOAuthCredentials(final AsyncWebContext context);

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "configuration", this.configuration);
    }

}

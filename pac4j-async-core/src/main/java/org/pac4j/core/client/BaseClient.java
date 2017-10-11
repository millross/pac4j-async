package org.pac4j.core.client;

import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class BaseClient <C extends Credentials, U extends CommonProfile, WC extends WebContext<?>, AG extends AuthorizationGenerator<WC, U>> extends CommonBaseClient<C, U, WC, AG>
        implements Client<C, U, WC> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CredentialsExtractor<C, WC> credentialsExtractor;

    private Authenticator<C, WC> authenticator;

    private ProfileCreator<C, U> profileCreator = AuthenticatorProfileCreator.INSTANCE;

    /**
     * Retrieve the credentials.
     *
     * @param context the web context
     * @return the credentials
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected C retrieveCredentials(final WC context) throws HttpAction {
        try {
            final C credentials = this.credentialsExtractor.extract(context);
            if (credentials == null) {
                return null;
            }
            this.authenticator.validate(credentials, context);
            return credentials;
        } catch (CredentialsException e) {
            logger.info("Failed to retrieve or validate credentials: {}", e.getMessage());
            logger.debug("Failed to retrieve or validate credentials", e);

            return null;
        }
    }

    @Override
    public final U getUserProfile(final C credentials, final WC context) throws HttpAction {
        init(context);
        logger.debug("credentials : {}", credentials);
        if (credentials == null) {
            return null;
        }

        U profile = retrieveUserProfile(credentials, context);
        if (profile != null) {
            profile.setClientName(getName());
            if (this.authorizationGenerators != null) {
                for (AG authorizationGenerator : this.authorizationGenerators) {
                    profile = authorizationGenerator.generate(context, profile);
                }
            }
        }
        return profile;
    }

    /**
     * Retrieve a user userprofile.
     *
     * @param credentials the credentials
     * @param context the web context
     * @return the user profile
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected final U retrieveUserProfile(final C credentials, final WC context) throws HttpAction {
        final U profile = this.profileCreator.create(credentials, context);
        logger.debug("profile: {}", profile);
        return profile;
    }

    public CredentialsExtractor<C, WC> getCredentialsExtractor() {
        return credentialsExtractor;
    }

    protected void defaultCredentialsExtractor(final CredentialsExtractor<C, WC> credentialsExtractor) {
        if (this.credentialsExtractor == null) {
            this.credentialsExtractor = credentialsExtractor;
        }
    }

    public Authenticator<C, WC> getAuthenticator() {
        return authenticator;
    }

    protected void defaultAuthenticator(final Authenticator<C, WC> authenticator) {
        if (this.authenticator == null) {
            this.authenticator = authenticator;
        }
    }

    public ProfileCreator<C, U> getProfileCreator() {
        return profileCreator;
    }

    protected void defaultProfileCreator(final ProfileCreator<C, U> profileCreator) {
        if (this.profileCreator == null || this.profileCreator == AuthenticatorProfileCreator.INSTANCE) {
            this.profileCreator = profileCreator;
        }
    }

    public void setCredentialsExtractor(final CredentialsExtractor<C, WC> credentialsExtractor) {
        this.credentialsExtractor = credentialsExtractor;
    }

    public void setAuthenticator(final Authenticator<C, WC> authenticator) {
        this.authenticator = authenticator;
    }

    public void setProfileCreator(final ProfileCreator<C, U> profileCreator) {
        this.profileCreator = profileCreator;
    }

    /**
     * Notify of the web session renewal.
     *
     * @param oldSessionId the old session identifier
     * @param context the web context
     */
    public void notifySessionRenewal(final String oldSessionId, final WC context) { }


    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "name", getName(), "credentialsExtractor", this.credentialsExtractor,
                "authenticator", this.authenticator, "profileCreator", this.profileCreator, "authorizationGenerators", authorizationGenerators);
    }

}

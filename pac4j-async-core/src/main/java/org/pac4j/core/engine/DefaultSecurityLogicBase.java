package org.pac4j.core.engine;

import org.pac4j.core.matching.DefaultMatchingChecker;
import org.pac4j.core.matching.MatchingChecker;
import org.pac4j.core.profile.ProfileManagerFactoryAware;

/**
 * Common base class for the async and sync security logic classes. Note that this only incorporates methods which
 * we can be reasonably sure will not be blocking. I think it's highly likely that authorization checking could be
 * blocking (e.g. via JDBC lookup) so we will need an async api for authorization checking. While ClientFinder is not
 * blocking, its implementation will depend (thanks to typing) on whether or not we are using sync or async clients
 * for now I'm going to use a specific new ClientFinder definition but it's possible we could genericise the main
 * implementation instead
 *
 * Type parameters in this class:-
 * B - base class for clients which will be used by the security logic (either Client for sync API or AsyncClient
 * for async api)
 * C - pac4j context type to be consumed by the security logic
 * R - the type of http result to return (may be Void in some cases)
 */
public abstract class DefaultSecurityLogicBase<B, C, L> extends ProfileManagerFactoryAware{

    private boolean saveProfileInSession;
    private MatchingChecker matchingChecker = new DefaultMatchingChecker();

    public MatchingChecker getMatchingChecker() {
        return matchingChecker;
    }

    public void setMatchingChecker(final MatchingChecker matchingChecker) {
        this.matchingChecker = matchingChecker;
    }

    public boolean isSaveProfileInSession() {
        return saveProfileInSession;
    }

    public void setSaveProfileInSession(final boolean saveProfileInSession) {
        this.saveProfileInSession = saveProfileInSession;
    }


}

package org.pac4j.async.core;

import org.pac4j.core.profile.CommonProfile;

/**
 * Trivial profile for some very simple testing of core infrastructure
 */
public class TestProfile extends CommonProfile {

    public static TestProfile from(final TestCredentials credentials) {
        return new TestProfile(credentials.getName());
    }

    private TestProfile(String name) {
        this.setId(name);
    }
}

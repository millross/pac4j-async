package org.pac4j.async.core;

import org.pac4j.core.credentials.Credentials;

/**
 * Quick demo credentials. Note that these are not
 */
public class TestCredentials extends Credentials {

    private final String name;
    private final String password;

    public TestCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {

        if (! (o instanceof TestCredentials)) {
            return false;
        }

        final TestCredentials other = (TestCredentials) o;

        return this.name == other.name && this.password == other.password;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}

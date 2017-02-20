package org.pac4j.async.core;

/**
 * Checked version of intentional exception to ensure that we also propogate checked exceptions correctly
 */
public class CheckedIntentionalException extends Exception {
    public static Integer throwException() throws CheckedIntentionalException {
        throw new CheckedIntentionalException();
    }

    public CheckedIntentionalException() {
        super("Checked intentional exception");
    }

}

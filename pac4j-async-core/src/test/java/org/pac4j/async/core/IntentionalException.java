package org.pac4j.async.core;

/**
 * Simple Intentional Exception class definition for async exception handling testing
 */
public class IntentionalException extends RuntimeException {

    public static Integer throwException(final Integer i) {
        throw new IntentionalException();
    }

    public IntentionalException() {
            super("Intentional exception");
        }

}

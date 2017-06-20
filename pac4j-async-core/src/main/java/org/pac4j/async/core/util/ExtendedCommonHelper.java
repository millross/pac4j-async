package org.pac4j.async.core.util;

import org.pac4j.core.exception.TechnicalException;

import java.util.Optional;

import static org.pac4j.core.util.CommonHelper.assertTrue;

/**
 *
 */
public class ExtendedCommonHelper {
    /**
     * Verify that an Optional contains a value otherwise throw a {@link TechnicalException}.
     *
     * @param name name of the object
     * @param opt  object Optional being checked
     */
    public static <T> void assertPresent(final String name, final Optional<T> opt) {
        assertTrue(opt.isPresent(), name + " cannot be null");
    }

}

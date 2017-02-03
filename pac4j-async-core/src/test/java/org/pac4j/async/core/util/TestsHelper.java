package org.pac4j.async.core.util;

import org.junit.Assert;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.InitializableObject;

/**
 * This class is an helper for tests: get a basic web client, parameters from an url, a formatted date, etc.
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class TestsHelper {

    public static void initShouldFail(final InitializableObject obj, final String message) {
        expectException(() -> obj.init(), TechnicalException.class, message);
    }

    public static Exception expectException(final Runnable executable) {
        try {
            executable.run();
        } catch (final Exception e) {
            return e;
        }
        return null;
    }

    public static void expectException(final Runnable executable, final Class<? extends Exception> clazz, final String message) {
        final Exception e = expectException(executable);
        Assert.assertTrue(clazz.isAssignableFrom(e.getClass()));
        Assert.assertEquals(message, e.getMessage());
    }
}
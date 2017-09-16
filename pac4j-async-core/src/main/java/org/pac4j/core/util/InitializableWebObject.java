package org.pac4j.core.util;

import org.pac4j.core.context.WebContextBase;

/**
 * Mirroring the sync version for now to allow delayed initialization, though we expect to move away from this
 * sooner rather than later
 */
public abstract class InitializableWebObject<T extends WebContextBase<?>> {
    private volatile boolean initialized = false;

    /**
     * Initialize the object.
     *
     * @param context the web context
     */
    public void init(final T context) {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    internalInit(context);
                    this.initialized = true;
                }
            }
        }
    }

    /**
     * Force (again) the initialization of the object.
     *
     * @param context the web context
     */
    public synchronized void reinit(final T context) {
        internalInit(context);
        this.initialized = true;
    }

    /**
     * Internal initialization of the object.
     *
     * @param context the web context
     */
    protected abstract void internalInit(final T context);
}

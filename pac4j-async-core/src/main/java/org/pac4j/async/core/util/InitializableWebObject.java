package org.pac4j.async.core.util;

/**
 * InitializableWebObject class not coupled to WebContext and whose initialization isn't coupled to a WebContext
 * either (it shouldn't be)
 */
public abstract class InitializableWebObject {

    private volatile boolean initialized = false;

    /**
     * Initialize the object.
     *
     */
    public void init() {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    internalInit();
                    this.initialized = true;
                }
            }
        }
    }

    /**
     * Force (again) the initialization of the object.
     *
     */
    public synchronized void reinit() {
        internalInit();
        this.initialized = true;
    }

    /**
     * Internal initialization of the object.
     *
     */
    protected abstract void internalInit();

}

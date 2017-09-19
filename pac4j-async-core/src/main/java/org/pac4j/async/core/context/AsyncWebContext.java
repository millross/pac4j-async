package org.pac4j.async.core.context;

import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.WebContext;

/**
 *
 */
public interface AsyncWebContext extends WebContext<AsyncSessionStore> {

    /**
     * Get the session store.
     *
     * @return the session store
     */
    default <T extends AsyncSessionStore>  T getSessionStore() {
        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Set the session store.
     *
     * @param sessionStore the session store
     */
    default <T extends AsyncSessionStore> void setSessionStore(T sessionStore) {
        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Get the execution context which was in force when this web context was created. This is so that for context-aware
     * frameworks such as vert.x we will always access this web context from the right context (fulfilling threading
     * guarantees made by the framework). Note that there is a ContextFreePac4jExecutionContext where the implementing
     * framework does not relate threads to contexts.
     *
     * This method should generally not be overridden because we want the async adapter and the async web context to use
     * the same underlying framework context (to respect threading, exception handling etc guarantees)
     * @return
     */
    default AsyncPac4jExecutionContext getExecutionContext() {
        return getAsyncComputationAdapter().getExecutionContext();
    }

    /**
     * Get the asynchronous computation adapter (which is also based on current execution context) for the AsyncWebContext
     * This is primarily used for converting blocking sync code into code which will run correctly in an async manner
     * on the context.
     */
    AsynchronousComputationAdapter getAsyncComputationAdapter();
}

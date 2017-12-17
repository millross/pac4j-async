package org.pac4j.async.vertx

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

/**
 *
 */
class SessionCookieHolder {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SessionCookieHolder::class.java)
    }

    private val sessionCookie = AtomicReference<String>()

    fun reset() {
        LOG.info("Resetting session cookie")
        sessionCookie.set(null)
    }

    fun retrieve(): String?  {
        LOG.info("Retrieving session cookie ${sessionCookie.get()}")
        return sessionCookie.get()
    }

    fun persist(cookie: String)  {

        LOG.info("Session cookie is ${sessionCookie.get()}")
        // Only bother setting it if not already set
        if(sessionCookie.get() == null) {
            LOG.info("Setting session cookie $cookie")
            sessionCookie.set(cookie)
        }
    }


}
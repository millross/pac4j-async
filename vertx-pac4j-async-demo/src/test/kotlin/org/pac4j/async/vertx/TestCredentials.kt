package org.pac4j.async.vertx

import org.pac4j.core.credentials.Credentials

/**
 *
 */
class TestCredentials(val name: String, val password: String): Credentials() {

    override fun equals(o: Any?): Boolean {

        if (o !is TestCredentials) {
            return false
        }

        val other = o as TestCredentials?

        return this.name === other!!.name && this.password === other!!.password
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }


}
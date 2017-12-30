package org.pac4j.async.vertx.profile.indirect

import org.pac4j.core.credentials.Credentials

/**
 *
 */
class TestOAuthCredentials(val name: String, val password: String): Credentials() {

    override fun equals(o: Any?): Boolean {

        if (o !is TestOAuthCredentials) {
            return false
        }

        val other = o as TestOAuthCredentials?

        return this.name === other!!.name && this.password === other!!.password
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }


}
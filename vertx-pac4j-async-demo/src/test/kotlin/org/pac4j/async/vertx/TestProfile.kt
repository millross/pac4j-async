package org.pac4j.async.vertx

import org.pac4j.core.profile.CommonProfile

/**
 *
 */
class TestProfile(name: String): CommonProfile() {

    companion object {
        fun from(credentials: TestCredentials): TestProfile {
            return TestProfile(credentials.name)
        }
    }

    init {
        setId(name)
    }

    override fun equals(that: Any?): Boolean {
        if (that !is TestProfile) return false
        val other = that as TestProfile?
        return this.id === other!!.id
    }

}
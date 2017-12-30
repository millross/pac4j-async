package org.pac4j.async.vertx.profile.direct

import org.pac4j.async.vertx.FIELD_EMAIL
import org.pac4j.async.vertx.FIELD_USER_ID
import org.pac4j.core.profile.CommonProfile

/**
 *
 */
class TestProfile(name: String): CommonProfile() {

    companion object {
        fun from(credentials: TestCredentials): TestProfile {
            return TestProfile(credentials.userId)
        }
    }

    class SimpleTestProfile(userId: String?, email: String?): CommonProfile() {

        init {
            super.setId(userId)
            super.addAttribute(FIELD_EMAIL, email)
            super.addAttribute(FIELD_USER_ID, userId)
        }

    }

    override fun equals(that: Any?): Boolean {
        if (that !is TestProfile) return false
        val other = that as TestProfile?
        return this.id === other!!.id
                && this.getAttribute(FIELD_EMAIL) === other!!.getAttribute(FIELD_EMAIL)
                && this.getAttribute(FIELD_USER_ID) === other!!.getAttribute(FIELD_USER_ID)
    }

}
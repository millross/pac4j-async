package org.pac4j.async.vertx.profile.indirect

import org.pac4j.oauth.profile.OAuth20Profile

/**
 *
 */
class TestOAuth20Profile(name: String): OAuth20Profile() {

    companion object {
        fun from(credentials: TestOAuthCredentials): TestOAuth20Profile {
            return TestOAuth20Profile(credentials.name)
        }
    }

    init {
        setId(name)
    }

    override fun equals(that: Any?): Boolean {
        if (that !is TestOAuth20Profile) return false
        val other = that as TestOAuth20Profile?
        return this.id === other!!.id
    }

}
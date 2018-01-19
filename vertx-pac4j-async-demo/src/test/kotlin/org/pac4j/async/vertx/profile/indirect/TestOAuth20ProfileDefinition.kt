package org.pac4j.async.vertx.profile.indirect

import io.vertx.core.json.JsonObject
import org.pac4j.async.oauth.profile.definition.OAuth20ProfileDefinition
import org.pac4j.async.vertx.EMAIL_KEY
import org.pac4j.async.vertx.USER_ID_KEY

/**
 *
 */
class TestOAuth20ProfileDefinition : OAuth20ProfileDefinition<TestOAuth20Profile>() {

    override fun extractUserProfile(body: String?): TestOAuth20Profile {
        val json = JsonObject(body)
        val userId = json.getString(USER_ID_KEY)
        val email = json.getString(EMAIL_KEY)
        val profile = TestOAuth20Profile(userId)
        profile.addAttribute(EMAIL_KEY, email)
        return profile
    }

}
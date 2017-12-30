package org.pac4j.async.vertx.profile.indirect

import org.pac4j.async.oauth.profile.definition.OAuth20ProfileDefinition
import org.pac4j.async.vertx.TEST_CREDENTIALS

/**
 *
 */
class TestOAuth20ProfileDefinition : OAuth20ProfileDefinition<TestOAuth20Profile>() {

    override fun extractUserProfile(body: String?): TestOAuth20Profile {
        return TestOAuth20Profile.from(TEST_CREDENTIALS)
    }

}
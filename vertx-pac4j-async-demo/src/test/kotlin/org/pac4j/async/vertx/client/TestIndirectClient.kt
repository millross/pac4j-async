package org.pac4j.async.vertx.client

import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.oauth.client.AsyncOAuth20Client
import org.pac4j.async.oauth.config.OAuth20Configuration
import org.pac4j.async.vertx.profile.TestOAuth20Profile
import org.pac4j.async.vertx.profile.TestOAuth20ProfileDefinition
import org.pac4j.async.vertx.profile.TestUrlProfileCalculator
import java.util.*

/**
 *
 */
class TestIndirectClient: AsyncOAuth20Client<TestOAuth20Profile, OAuth20Configuration<TestOAuth20Profile, TestOAuth20ProfileDefinition>, TestUrlProfileCalculator>() {

    var baseAuthorizationUrl: String = "http://localhost:9292"

    init {
        setConfiguration(OAuth20Configuration())
        configuration.key = UUID.randomUUID().toString()
        configuration.secret = UUID.randomUUID().toString()
    }

    override fun notifySessionRenewal(oldSessionId: String?, context: AsyncWebContext) {
        // Intentional noop
    }

    override fun clientInit(context: AsyncWebContext?) {
        configuration.api = TestOAuth20Api(baseAuthorizationUrl)
        configuration.setProfileDefinition(TestOAuth20ProfileDefinition())
        configuration.isWithState = true
        super.clientInit(context)
    }
}
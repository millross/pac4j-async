package org.pac4j.async.vertx.client

import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.oauth.client.AsyncOAuth20Client
import org.pac4j.async.oauth.config.OAuth20Configuration
import org.pac4j.async.vertx.AUTH_BASE_URL
import org.pac4j.async.vertx.TEST_CLIENT_ID
import org.pac4j.async.vertx.TEST_CLIENT_SECRET
import org.pac4j.async.vertx.profile.indirect.TestOAuth20Profile
import org.pac4j.async.vertx.profile.indirect.TestOAuth20ProfileDefinition
import org.pac4j.async.vertx.profile.indirect.TestOAuthCredentialsExtractor
import org.pac4j.async.vertx.profile.indirect.TestUrlProfileCalculator

/**
 *
 */
class TestIndirectClient: AsyncOAuth20Client<TestOAuth20Profile, OAuth20Configuration<TestOAuth20Profile, TestOAuth20ProfileDefinition>, TestUrlProfileCalculator>() {

    var baseAuthorizationUrl = AUTH_BASE_URL

    init {
        setConfiguration(OAuth20Configuration())
        configuration.key = TEST_CLIENT_ID
        configuration.secret = TEST_CLIENT_SECRET
    }

    override fun notifySessionRenewal(oldSessionId: String?, context: AsyncWebContext) {
        // Intentional noop
    }

    override fun clientInit(context: AsyncWebContext?) {
        configuration.api = TestOAuth20Api(baseAuthorizationUrl)
        configuration.setProfileDefinition(TestOAuth20ProfileDefinition())
        configuration.isWithState = true
        defaultCredentialsExtractor(TestOAuthCredentialsExtractor())
        super.clientInit(context)
    }
}
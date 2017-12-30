package org.pac4j.async.vertx.profile.indirect

import com.github.scribejava.core.model.OAuth2AccessToken
import org.pac4j.async.oauth.config.OAuth20Configuration
import org.pac4j.async.oauth.profile.url.OAuthProfileUrlCalculator

/**
 *
 */
class TestUrlProfileCalculator: OAuthProfileUrlCalculator<OAuth2AccessToken, OAuth20Configuration<TestOAuth20Profile, TestOAuth20ProfileDefinition>>() {
    override fun getProfileUrl(accessToken: OAuth2AccessToken?, configuration: OAuth20Configuration<TestOAuth20Profile, TestOAuth20ProfileDefinition>?): String {
        return "http://localhost:9292/profile"
    }
}
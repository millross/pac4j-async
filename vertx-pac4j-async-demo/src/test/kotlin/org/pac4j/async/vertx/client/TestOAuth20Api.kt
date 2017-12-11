package org.pac4j.async.vertx.client

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.model.Verb

/**
 * Simple api to be used by our indirect client
 */
class TestOAuth20Api(val baseAuthorizationUrl: String): DefaultApi20() {

    override fun getAccessTokenEndpoint(): String {
        return "http://localhost:9292/authToken?grant_type=authorization_code"
    }

    override fun getAuthorizationBaseUrl(): String {
        return this.baseAuthorizationUrl
    }

    override fun getAccessTokenVerb(): Verb {
        return Verb.GET
    }

}
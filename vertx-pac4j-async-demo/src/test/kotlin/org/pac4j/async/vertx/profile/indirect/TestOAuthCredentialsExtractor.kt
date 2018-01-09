package org.pac4j.async.vertx.profile.indirect

import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.core.credentials.extractor.AsyncCredentialsExtractor
import org.pac4j.async.vertx.TEST_CREDENTIALS
import org.pac4j.oauth.credentials.OAuth20Credentials
import java.util.concurrent.CompletableFuture

/**
 *
 */
class TestOAuthCredentialsExtractor: AsyncCredentialsExtractor<OAuth20Credentials> {
    override fun extract(context: AsyncWebContext?): CompletableFuture<OAuth20Credentials> {
        return CompletableFuture.completedFuture(TEST_CREDENTIALS)
    }
}
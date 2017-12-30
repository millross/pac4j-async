package org.pac4j.async.vertx.client

import org.pac4j.async.core.client.AsyncDirectClient
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.vertx.HEADER_EMAIL
import org.pac4j.async.vertx.HEADER_USER_ID
import org.pac4j.async.vertx.profile.direct.TestCredentials
import org.pac4j.async.vertx.profile.direct.TestProfile
import org.pac4j.core.exception.HttpAction
import org.pac4j.core.redirect.RedirectAction
import java.util.concurrent.CompletableFuture
import javax.security.auth.login.CredentialException
import kotlin.UnsupportedOperationException

/**
 *
 */
class TestDirectClient (matchingPattern: String): AsyncDirectClient<TestCredentials, TestProfile>() {

    init {
        val matchingRegExp = Regex(matchingPattern)
        name = matchingPattern.toLowerCase()
        setCredentialsExtractor { context ->
            CompletableFuture.completedFuture(context.getRequestHeader("Authorization") ?: "")
                    .thenApply { authHeader: String ->
                        if (matchingRegExp.containsMatchIn(authHeader)) TestCredentials(context.getRequestHeader(HEADER_USER_ID),
                                context.getRequestHeader(HEADER_EMAIL)) else null
                    }
        }
        setAuthenticator { credentials, webContext ->
            CompletableFuture.completedFuture(null)
                    .thenApply { v ->
                        if (credentials == null || credentials.userId == null || credentials.email == null) {
                            throw CredentialException("Authorization header does not pass authentication")
                        } else {
                            null
                        }
                    }
        }
        setProfileCreator { credentials, webContext ->  CompletableFuture.completedFuture(TestProfile.from(credentials)) }
    }

    override fun internalInit(context: AsyncWebContext?) {
    }

    override fun redirect(context: AsyncWebContext?): CompletableFuture<HttpAction> {
        throw UnsupportedOperationException()
    }

    override fun getLogoutAction(var1: AsyncWebContext?, var2: TestProfile?, var3: String?): RedirectAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
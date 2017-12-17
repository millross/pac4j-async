package org.pac4j.async.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Test client to wrap a set of http calls to our test server. We will also record any state (such as session cookies)
 * within this client for reuse
 */
class TestClient(val client: WebClient) {

    val cookieHolder: SessionCookieHolder = SessionCookieHolder()

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(TestClient.javaClass)
    }

    suspend fun retrievingSessionCookie(processing: suspend () -> HttpResponse<Buffer>): HttpResponse<Buffer> {
        val response = processing()
        val cookie = response.headers().get("set-cookie")
        cookieHolder.persist(cookie)
        return response
    }

    suspend fun usingSessionCookie(processing: suspend () -> HttpRequest<Buffer>): HttpRequest<Buffer> {
        val request = processing()
        if (cookieHolder.retrieve() != null) {
            request.headers().set("cookie", cookieHolder.retrieve())
        }
        return request
    }

    suspend fun spoofLogin() {

        LOG.info("Spoofing login")
        val request = usingSessionCookie { client.post(8080, "localhost", "/spoofLogin") }
        val response: HttpResponse<Buffer> = retrievingSessionCookie {
            awaitResult { request.send(it) }
        }
        if (response.statusCode() != 204) {
            // We should fail now
            LOG.info("statusCode: " + response.statusCode())
            throw RuntimeException("Failed to spoof login")
        }
        LOG.info("spoofLogin succeeded")
    }

    suspend fun getSecuredEndpoint(): HttpResponse<Buffer> {
        LOG.info("Retrieving secured endpoint§")
        val request = usingSessionCookie { client.get(8080, "localhost", "/profile")}
        return awaitResult { request.send(it) }
    }

}
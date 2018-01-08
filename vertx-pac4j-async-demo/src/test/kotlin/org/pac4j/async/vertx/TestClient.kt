package org.pac4j.async.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Test client to wrap a set of http calls to our test server. We will also record any state (such as session cookies)
 * within this client for reuse
 */
class TestClient(val vertx: Vertx) {

    val cookieHolder: SessionCookieHolder = SessionCookieHolder()
    val client: WebClient = WebClient.create(vertx, WebClientOptions().setFollowRedirects(false))

    /**
     * Decorator for requests to secured endpoint. Aim being to enable intentional authentication success or
     * failure on the test direct client, for known test scenarios.
     */
    var requestDecorator: (HttpRequest<Buffer>) -> HttpRequest<Buffer> = { request: HttpRequest<Buffer> -> request }

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

    suspend fun simulateThirdPartyLoginViaRedirect() {
        LOG.info("Spoofing successul third party login")
        val request = usingSessionCookie { client.post(8080, "localhost", "/simulateOAuthLogin") }
        val response: HttpResponse<Buffer> = retrievingSessionCookie {
            awaitResult { request.send(it) }
        }
        if (response.statusCode() != 204) {
            // We should fail now
            LOG.info("statusCode: " + response.statusCode())
            throw RuntimeException("Failed to simulate third party login")
        }
        LOG.info("Third party login simulation succeeded")
    }

    suspend fun getCallbackUrl() {
        LOG.info("Attempting to GET callback url")
        val request = usingSessionCookie {
            client.post(8080, "localhost", "/callback?")
                .addQueryParam("client_name", TEST_CLIENT_NAME)
        }
        val response: HttpResponse<Buffer> = retrievingSessionCookie {
            awaitResult { request.send(it) }
        }
        if (response.statusCode() != 302) {
            // We should fail now
            LOG.info("statusCode: " + response.statusCode())
            throw RuntimeException("Failed to GET callback url")
        }
        LOG.info("Callback invocation failed")
    }

    suspend fun getSecuredEndpoint(): HttpResponse<Buffer> {
        LOG.info("Retrieving secured endpoint§")
        val request = usingSessionCookie { requestDecorator(client.get(8080, "localhost", "/profile")) }
        requestDecorator = { request -> request }
        return awaitResult { request.send(it) }
    }

    suspend fun withRequestDecorator(decorator: ((HttpRequest<Buffer>) -> Unit)): TestClient {
        requestDecorator = { request ->
            decorator(request)
            request
        }
        return this
    }

}
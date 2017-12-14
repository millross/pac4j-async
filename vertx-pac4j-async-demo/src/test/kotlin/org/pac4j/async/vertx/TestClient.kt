package org.pac4j.async.vertx

import io.vertx.core.buffer.Buffer
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

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(TestClient.javaClass)
    }

    suspend fun spoofLogin() {

        val response = awaitResult<HttpResponse<Buffer>> {client.post(8080, "localhost", "/spoofLogin").send(it) }
        if (response.statusCode() != 204) {
            // We should fail now
            LOG.info("statusCode: " + response.statusCode())
            throw RuntimeException("Failed to spoof login")
        }
        LOG.info("spoofLogin succeeded")
    }

    suspend fun getSecuredEndpoint(): HttpResponse<Buffer> {
        return awaitResult {client.get(8080, "localhost", "/profile").send(it) }
    }

}
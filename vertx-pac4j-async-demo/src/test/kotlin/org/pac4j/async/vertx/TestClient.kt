package org.pac4j.async.vertx

import io.vertx.ext.web.client.WebClient

/**
 * Test client to wrap a set of http calls to our test server. We will also record any state (such as session cookies)
 * within this client for reuse
 */
class TestClient(val client: WebClient) {



}
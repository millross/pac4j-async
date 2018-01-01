package org.pac4j.async.vertx.config

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator
import org.pac4j.async.core.client.AsyncDirectClient
import org.pac4j.async.core.client.AsyncIndirectClient
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.vertx.CALLBACK_URL
import org.pac4j.async.vertx.TEST_CLIENT_NAME
import org.pac4j.async.vertx.client.TestDirectClient
import org.pac4j.async.vertx.client.TestIndirectClient
import org.pac4j.core.client.Clients
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile

/**
 * Factory class to be used for creating test configs based on direct/indirect clients
 */
class TestPac4jConfigFactory {

    fun indirectClientConfig(): AsyncConfig<Void, CommonProfile, AsyncWebContext> {
        val config = AsyncConfig<Void, CommonProfile, AsyncWebContext>()
        val client = TestIndirectClient()
        client.setName(TEST_CLIENT_NAME)
        val clients = Clients<AsyncIndirectClient<out Credentials, out CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>>(CALLBACK_URL, client)
        config.setClients(clients)
        return config
    }

    fun directClientConfig(): AsyncConfig<Void, CommonProfile, AsyncWebContext> {
        val config = AsyncConfig<Void, CommonProfile, AsyncWebContext>()
        val client = TestDirectClient("ABC")
        client.setName(TEST_CLIENT_NAME)
        val clients = Clients<AsyncDirectClient<out Credentials, out CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>>(CALLBACK_URL, client)
        config.setClients(clients)
        return config
    }

}
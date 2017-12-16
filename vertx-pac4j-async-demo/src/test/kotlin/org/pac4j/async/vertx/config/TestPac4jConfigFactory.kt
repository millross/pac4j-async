package org.pac4j.async.vertx.config

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator
import org.pac4j.async.core.client.AsyncIndirectClient
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.vertx.CALLBACK_URL
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
        val clients = Clients<AsyncIndirectClient<out Credentials, out CommonProfile>, AsyncAuthorizationGenerator<CommonProfile>>(CALLBACK_URL, TestIndirectClient())
        config.setClients(clients)
        return config
    }

}
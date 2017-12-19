package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.vertx.config.TestPac4jConfigFactory
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simple tests for security handler using kotlin as a convenient test mechanism
 */
@RunWith(VertxUnitRunner::class)
class AsyncSecurityHandlerTest {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(AsyncSecurityHandlerTest.javaClass)
    }

    @Rule
    @JvmField
    val rule = RunTestOnContext()

    val configFactory: TestPac4jConfigFactory = TestPac4jConfigFactory()

    @Test(timeout = 10000)
    fun testAlreadyLoggedIn(testContext: TestContext) {

        val async = testContext.async()
        val vertx = rule.vertx()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            val client = TestClient(WebClient.create(vertx))
            client.spoofLogin()
            // Now retrieve the private endpoint
            val response = client.getSecuredEndpoint()
            val status = response.statusCode()
            LOG.info("Status is " + status)
            val body = response.bodyAsJsonObject()
            assertThat(body.getString(USER_ID_KEY), `is`(GOOD_USERNAME))
            assertThat(body.getString(SESSION_ID_KEY), `is`(notNullValue()))
            assertThat(body.getString(EMAIL_KEY), `is`(TEST_EMAIL))

            async.complete()
        }
        async.await()
    }

    suspend fun startServer(vertx: Vertx, configuration: AsyncConfig<Void, CommonProfile, AsyncWebContext>) {
        TestServer(vertx).withPac4jConfiguration(configuration).start()
    }
}

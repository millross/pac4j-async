package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.experimental.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.vertx.config.TestPac4jConfigFactory
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.async.vertx.handler.SpoofLoginHandler
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simple test for the vertx async logout handler
 */
@RunWith(VertxUnitRunner::class)
class AsyncLogoutHandlerTest {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(AsyncLogoutHandlerTest.javaClass)
    }

    @Rule
    @JvmField
    val rule = RunTestOnContext()

    val configFactory: TestPac4jConfigFactory = TestPac4jConfigFactory()
    lateinit var vertx: Vertx

    @Before
    fun setExceptionHandler(testContext: TestContext) {
        vertx = rule.vertx()
        vertx.exceptionHandler(testContext.exceptionHandler())
    }

    @Test(timeout = 3000)
    fun testLogout(testContext: TestContext) {

        val async = testContext.async()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            val client = TestClient(vertx)
            client.spoofLogin()
            // Now trigger a logout
            client.logout()
            // Now retrieve the private endpoint
            val response = client.getSecuredEndpoint()
            validateRedirectToCallbackForPrivateEndpoint(response)
            async.complete()
        }
        async.await()
    }


    suspend fun startServer(vertx: Vertx, configuration: AsyncConfig<Void, CommonProfile, VertxAsyncWebContext>) {
        val context = vertx.orCreateContext
        TestServer(vertx)
                .withPac4jConfiguration(configuration)
                .withRoutingCustomization { router -> router.post("/spoofLogin").handler(SpoofLoginHandler(vertx, context)) }
                .start()
    }

}
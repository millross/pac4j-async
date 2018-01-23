package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.experimental.launch
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.vertx.authserver.DummyAuthServer
import org.pac4j.async.vertx.config.TestPac4jConfigFactory
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.async.vertx.handler.SimulateSuccessfulThirdPartyLoginHandler
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Test for callback handling
 */
@RunWith(VertxUnitRunner::class)
class AsyncCallbackHandlerTest {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(AsyncSecurityHandlerTest.javaClass)
    }

    @Rule
    @JvmField
    val rule = RunTestOnContext()

    val configFactory: TestPac4jConfigFactory = TestPac4jConfigFactory()

    lateinit  var vertx: Vertx

    @Before
    fun setExceptionHandler(testContext: TestContext) {
        vertx = rule.vertx()
        vertx.exceptionHandler(testContext.exceptionHandler())
    }

    @Test(timeout=3000)
    fun testCallbackWithAuthenticationSuccess(testContext: TestContext) {
        val async = testContext.async()
        val vertx = rule.vertx()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            DummyAuthServer(vertx).start()
            val client = TestClient(vertx)
            // Now retrieve the private endpoint without spoofing login, and without setting the headers up for login
            client.simulateThirdPartyLoginViaRedirect()

            // Now attempt to invoke the callback handler
            val response = client.getCallbackUrl()

            assertThat(response.statusCode(), `is`(HttpConstants.TEMP_REDIRECT))
            assertThat(response.getHeader(HttpConstants.LOCATION_HEADER), `is`("http://localhost:8080/profile"))

            // Now hit the secured endpoint as our profile should have been written to the session
            val securedEndpointResponse = client.getSecuredEndpoint()
            val status = securedEndpointResponse.statusCode()
            AsyncSecurityHandlerTest.LOG.info("Status is " + status)
            val body = securedEndpointResponse.bodyAsJsonObject()
            assertThat(body.getString(USER_ID_KEY), `is`(GOOD_USERNAME))
            assertThat(body.getString(SESSION_ID_KEY), `is`(CoreMatchers.notNullValue()))
            assertThat(body.getString(EMAIL_KEY), `is`(TEST_EMAIL))

            async.complete()
        }

    }

    suspend fun startServer(vertx: Vertx, configuration: AsyncConfig<Void, CommonProfile, VertxAsyncWebContext>) {
        val context = vertx.orCreateContext
        TestServer(vertx)
                .withPac4jConfiguration(configuration)
                .withRoutingCustomization { router -> router.post("/simulateOAuthLogin").handler(SimulateSuccessfulThirdPartyLoginHandler(vertx, context)) }
                .start()
    }

}
package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.experimental.launch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
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

    lateinit  var vertx: Vertx

    @Before
    fun setExceptionHandler(testContext: TestContext) {
        vertx = rule.vertx()
        vertx.exceptionHandler(testContext.exceptionHandler())
    }

    @Test(timeout = 3000)
    fun testAlreadyLoggedIn(testContext: TestContext) {

        val async = testContext.async()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            val client = TestClient(vertx)
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

    @Test(timeout = 3000)
    fun testRedirectUsingIndirectClient(testContext: TestContext) {

        val async = testContext.async()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            val client = TestClient(vertx)
            // Now retrieve the private endpoint without spoofing login
            val response = client.getSecuredEndpoint()
            validateRedirectToCallbackForPrivateEndpoint(response)
            async.complete()
        }
        async.await()
    }

    @Test(timeout=3000)
    fun testDirectClientAuthSuccess(testContext: TestContext) {
        val async = testContext.async()
        launch {
            startServer(vertx, configFactory.directClientConfig())
            val client = TestClient(vertx)
            // Now retrieve the private endpoint without spoofing login
            val response = client.withRequestDecorator { req ->
                with (req) {
                    headers().add("Authorization", "ABC")
                    headers().add(HEADER_USER_ID, GOOD_USERNAME)
                    headers().add(HEADER_EMAIL, TEST_EMAIL)
                }
            }.getSecuredEndpoint()

            with (response) {
                assertThat(statusCode(), `is`(200))
                with (bodyAsJsonObject()) {
                    assertThat(getString(USER_ID_KEY), `is`(GOOD_USERNAME))
                    assertThat(getString(SESSION_ID_KEY), `is`(notNullValue()))
                    assertThat(getString(EMAIL_KEY), `is`(TEST_EMAIL))
                }
            }
            async.complete()
        }
    }

    @Test(timeout=3000)
    fun testDirectClientAuthFailure(testContext: TestContext) {
        val async = testContext.async()
        val vertx = rule.vertx()
        launch {
            startServer(vertx, configFactory.directClientConfig())
            val client = TestClient(vertx)
            // Now retrieve the private endpoint without spoofing login, and without setting the headers up for login
            val response = client.getSecuredEndpoint()
            assertThat(response.statusCode(), `is`(401))
            async.complete()
        }
    }


    suspend fun startServer(vertx: Vertx, configuration: AsyncConfig<Void, CommonProfile, VertxAsyncWebContext>) {
        val context = vertx.orCreateContext
        TestServer(vertx)
                .withPac4jConfiguration(configuration)
                .withRoutingCustomization { router -> router.post("/spoofLogin").handler(SpoofLoginHandler(vertx, context)) }
                .start()
    }
}

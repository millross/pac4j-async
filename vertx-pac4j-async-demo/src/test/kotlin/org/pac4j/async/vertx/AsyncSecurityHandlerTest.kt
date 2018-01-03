package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.experimental.launch
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URIBuilder
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.oauth.config.OAuthConfiguration.RESPONSE_TYPE_CODE
import org.pac4j.async.vertx.config.TestPac4jConfigFactory
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.async.vertx.handler.SpoofLoginHandler
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.HttpConstants.SCHEME_HTTP
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import java.util.stream.Collectors

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

    @Test(timeout = 3000)
    fun testAlreadyLoggedIn(testContext: TestContext) {

        val async = testContext.async()
        val vertx = rule.vertx()
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
        val vertx = rule.vertx()
        launch {
            startServer(vertx, configFactory.indirectClientConfig())
            val client = TestClient(vertx)
            // Now retrieve the private endpoint without spoofing login
            val response = client.getSecuredEndpoint()
            val status = response.statusCode()
            LOG.info("Status is " + status)
            assertThat(status, `is`(HttpConstants.TEMP_REDIRECT))
            val location = response.getHeader(HttpConstants.LOCATION_HEADER)
            LOG.info("Location is $location")
            with(URIBuilder(location)) {
                assertThat(host, `is`(AUTH_SERVER_HOST))
                assertThat(scheme, `is`(SCHEME_HTTP))
                assertThat(port, `is`(AUTH_SERVER_PORT))
                assertThat(path, `is`(AUTH_SERVER_PATH))
                with (queryParams.stream()
                    .collect(Collectors.toMap({ p: NameValuePair -> p.name}, {p: NameValuePair -> p.value} ))) {
                        assertThat(get(QUERY_PARAM_RESPONSE_TYPE), `is`(RESPONSE_TYPE_CODE))
                        assertThat(get(QUERY_PARAM_CLIENT_ID), `is`(TEST_CLIENT_ID))
                        assertThat(get(QUERY_PARAM_STATE), `is`(notNullValue()))
                        val redirectUri = URLDecoder.decode(get(QUERY_PARAM_REDIRECT_URI), "UTF8")
                        LOG.info("RedirectUri: $redirectUri")
                        // Now examine the redirect url and validate correctness
                        with (URIBuilder(redirectUri)) {
                            assertThat(host, `is`(CALLBACK_URL_HOST))
                            assertThat(scheme, `is`(SCHEME_HTTP))
                            assertThat(port, `is`(CALLBACK_URL_PORT))
                            assertThat(path, `is`(CALLBACK_URL_PATH))
                            with (queryParams.stream()
                                    .collect(Collectors.toMap({ p: NameValuePair -> p.name}, {p: NameValuePair -> p.value} ))) {
                                assertThat(get(QUERY_PARAM_CLIENT_NAME), `is`(TEST_CLIENT_NAME))
                            }
                        }
                    }
            }
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

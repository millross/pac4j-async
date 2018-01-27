package org.pac4j.async.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URIBuilder
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.pac4j.async.oauth.config.OAuthConfiguration
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.core.context.HttpConstants
import java.net.URLDecoder
import java.util.stream.Collectors

/**
 * Functions used by tests
 */
fun getProfileManager(rc: RoutingContext,
                      asynchronousComputationAdapter: VertxAsynchronousComputationAdapter): VertxAsyncProfileManager<VertxAsyncWebContext> {
    val webContext = VertxAsyncWebContext(rc, asynchronousComputationAdapter)
    return VertxAsyncProfileManager(webContext)

}

fun validateRedirectToCallbackForPrivateEndpoint(response: HttpResponse<Buffer>) {
    val status = response.statusCode()
    AsyncSecurityHandlerTest.LOG.info("Status is " + status)
    MatcherAssert.assertThat(status, CoreMatchers.`is`(HttpConstants.TEMP_REDIRECT))
    val location = response.getHeader(HttpConstants.LOCATION_HEADER)
    AsyncSecurityHandlerTest.LOG.info("Location is $location")
    with(URIBuilder(location)) {
        MatcherAssert.assertThat(host, CoreMatchers.`is`(AUTH_SERVER_HOST))
        MatcherAssert.assertThat(scheme, CoreMatchers.`is`(HttpConstants.SCHEME_HTTP))
        MatcherAssert.assertThat(port, CoreMatchers.`is`(AUTH_SERVER_PORT))
        MatcherAssert.assertThat(path, CoreMatchers.`is`(AUTH_SERVER_PATH))
        with(queryParams.stream()
                .collect(Collectors.toMap({ p: NameValuePair -> p.name }, { p: NameValuePair -> p.value }))) {
            MatcherAssert.assertThat(get(QUERY_PARAM_RESPONSE_TYPE), CoreMatchers.`is`(OAuthConfiguration.RESPONSE_TYPE_CODE))
            MatcherAssert.assertThat(get(QUERY_PARAM_CLIENT_ID), CoreMatchers.`is`(TEST_CLIENT_ID))
            MatcherAssert.assertThat(get(QUERY_PARAM_STATE), CoreMatchers.`is`(CoreMatchers.notNullValue()))
            val redirectUri = URLDecoder.decode(get(QUERY_PARAM_REDIRECT_URI), "UTF8")
            AsyncSecurityHandlerTest.LOG.info("RedirectUri: $redirectUri")
            // Now examine the redirect url and validate correctness
            with(URIBuilder(redirectUri)) {
                MatcherAssert.assertThat(host, CoreMatchers.`is`(CALLBACK_URL_HOST))
                MatcherAssert.assertThat(scheme, CoreMatchers.`is`(HttpConstants.SCHEME_HTTP))
                MatcherAssert.assertThat(port, CoreMatchers.`is`(CALLBACK_URL_PORT))
                MatcherAssert.assertThat(path, CoreMatchers.`is`(CALLBACK_URL_PATH))
                with(queryParams.stream()
                        .collect(Collectors.toMap({ p: NameValuePair -> p.name }, { p: NameValuePair -> p.value }))) {
                    MatcherAssert.assertThat(get(QUERY_PARAM_CLIENT_NAME), CoreMatchers.`is`(TEST_CLIENT_NAME))
                }
            }
        }
    }
}




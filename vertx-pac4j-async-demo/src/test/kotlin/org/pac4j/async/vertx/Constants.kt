package org.pac4j.async.vertx

import org.pac4j.async.vertx.profile.indirect.TestOAuthCredentials
import org.pac4j.core.context.HttpConstants.SCHEME_HTTP
import java.util.*

/**
 *
 */
const val GOOD_USERNAME = "jle"
const val PASSWORD = "password"
val TEST_CREDENTIALS = TestOAuthCredentials(GOOD_USERNAME, PASSWORD)
const val USER_ID_KEY = "userId"
const val EMAIL_KEY = "email"
const val SESSION_ID_KEY = "session_id"
const val TEST_CLIENT_NAME = "testClient"
const val TEST_EMAIL = "test@example.com"
const val QUERY_PARAM_RESPONSE_TYPE = "response_type"
const val QUERY_PARAM_CLIENT_ID = "client_id"
const val QUERY_PARAM_CLIENT_NAME = "client_name"
const val QUERY_PARAM_STATE = "state"
const val QUERY_PARAM_REDIRECT_URI = "redirect_uri"
const val CALLBACK_URL_HOST = "localhost"
const val CALLBACK_URL_PORT = 8080
const val CALLBACK_URL_PATH = "/callback"
const val CALLBACK_URL = "http://$CALLBACK_URL_HOST:$CALLBACK_URL_PORT$CALLBACK_URL_PATH"
const val AUTH_SERVER_HOST = "localhost"
const val AUTH_SERVER_PORT = 9292
const val AUTH_SERVER_PATH = "/auth"
const val AUTH_BASE_URL = "$SCHEME_HTTP://$AUTH_SERVER_HOST:$AUTH_SERVER_PORT$AUTH_SERVER_PATH"
val TEST_CLIENT_ID = UUID.randomUUID().toString()
val TEST_CLIENT_SECRET = UUID.randomUUID().toString()
const val FIELD_EMAIL = "email"
const val FIELD_USER_ID = "userId"
const val HEADER_USER_ID = "userId"
const val HEADER_EMAIL = "email"


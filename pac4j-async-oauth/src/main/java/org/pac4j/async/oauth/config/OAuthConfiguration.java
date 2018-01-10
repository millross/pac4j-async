package org.pac4j.async.oauth.config;

import com.github.scribejava.core.builder.api.BaseApi;
import com.github.scribejava.core.model.HttpClient;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.SignatureType;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.httpclient.ahc.AhcHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.pac4j.async.oauth.profile.definition.OAuthProfileDefinition;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.UrlResolver;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.HttpUtils;
import org.pac4j.core.util.InitializableWebObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public class OAuthConfiguration<U extends CommonProfile, P extends OAuthProfileDefinition<U>,
        S extends OAuthService<T>, T extends Token> extends InitializableWebObject<WebContext<?>> {

    public static final String OAUTH_TOKEN = "oauth_token";

    public static final String RESPONSE_TYPE_CODE = "code";

    private UrlResolver urlResolver;

    private String callbackUrl;

    private String key;

    private String secret;

    private boolean tokenAsHeader;

    private String responseType = RESPONSE_TYPE_CODE;

    private String scope;

    private BaseApi<S> api;

    private boolean hasGrantType;

    private Function<WebContext<?>, Boolean> hasBeenCancelledFactory = ctx -> false;

    private P profileDefinition;

    protected S service;
    private String clientName;

    private Supplier<HttpClient.Config> configSupplier = () -> new AhcHttpClientConfig(new DefaultAsyncHttpClientConfig.Builder().build());

    @Override
    protected void internalInit(final WebContext<?> context) {
        CommonHelper.assertNotNull("urlResolver", this.urlResolver);
        CommonHelper.assertNotNull("callbackUrl", this.callbackUrl);
        CommonHelper.assertNotBlank("key", this.key);
        CommonHelper.assertNotBlank("secret", this.secret);
        CommonHelper.assertNotNull("api", api);
        CommonHelper.assertNotNull("hasBeenCancelledFactory", hasBeenCancelledFactory);
        CommonHelper.assertNotNull("profileDefinition", profileDefinition);

        this.service = buildService(context, null);
    }

    /**
     * Build an OAuth service from the web context and with a state.
     *
     * @param context the web context
     * @param state a given state
     * @return the OAuth service
     */
    public S buildService(final WebContext<?> context, final String state) {
        return getApi().createService(buildOAuthConfig(context, state));
    }

    protected OAuthConfig buildOAuthConfig(final WebContext<?> context, final String state) {

        final String finalCallbackUrl = urlResolver.compute(callbackUrl, context);

        return new OAuthConfig(this.key, this.secret, finalCallbackUrl, SignatureType.Header, this.scope,
                null, state, this.responseType, null, HttpUtils.getConnectTimeout(), HttpUtils.getReadTimeout(),
                configSupplier.get(), null);
    }

    public S getService() {
        return this.service;
    }


    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public boolean isTokenAsHeader() {
        return tokenAsHeader;
    }

    public void setTokenAsHeader(final boolean tokenAsHeader) {
        this.tokenAsHeader = tokenAsHeader;
    }

    @Deprecated
    public int getConnectTimeout() {
        return HttpUtils.getConnectTimeout();
    }

    @Deprecated
    public void setConnectTimeout(final int connectTimeout) {
        HttpUtils.setConnectTimeout(connectTimeout);
    }

    @Deprecated
    public int getReadTimeout() {
        return HttpUtils.getReadTimeout();
    }

    @Deprecated
    public void setReadTimeout(final int readTimeout) {
        HttpUtils.setReadTimeout(readTimeout);
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(final String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public BaseApi<S> getApi() {
        return api;
    }

    public void setApi(final BaseApi<S> api) {
        this.api = api;
    }

    public boolean isHasGrantType() {
        return hasGrantType;
    }

    public void setHasGrantType(final boolean hasGrantType) {
        this.hasGrantType = hasGrantType;
    }

    public Function<WebContext<?>, Boolean> getHasBeenCancelledFactory() {
        return hasBeenCancelledFactory;
    }

    public void setHasBeenCancelledFactory(final Function<WebContext<?>, Boolean> hasBeenCancelledFactory) {
        this.hasBeenCancelledFactory = hasBeenCancelledFactory;
    }

    public P getProfileDefinition() {
        return profileDefinition;
    }

    public void setProfileDefinition(final P profileDefinition) {
        this.profileDefinition = profileDefinition;
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "key", key, "secret", "[protected]", "tokenAsHeader", tokenAsHeader,
                "connectTimeout", HttpUtils.getConnectTimeout(), "readTimeout", HttpUtils.getReadTimeout(), "responseType", responseType,
                "scope", scope, "api", api, "hasGrantType", hasGrantType, "service", service,
                "hasBeenCancelledFactory", hasBeenCancelledFactory, "profileDefinition", profileDefinition);
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public UrlResolver getUrlResolver() {
        return urlResolver;
    }

    public void setUrlResolver(UrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setConfigSupplier(Supplier<HttpClient.Config> newSupplier) {
        this.configSupplier = newSupplier;
    }

}

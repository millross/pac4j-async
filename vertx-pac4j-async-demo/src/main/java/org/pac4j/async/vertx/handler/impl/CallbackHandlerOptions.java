package org.pac4j.async.vertx.handler.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Vert.x-style options class for a callback handler
 * @since 2.0.0
 */
@Accessors(chain=true)
public class CallbackHandlerOptions {

    @Getter @Setter
    private String defaultUrl;

    @Getter @Setter
    private Boolean multiProfile;

    @Getter @Setter
    private Boolean renewSession;

}
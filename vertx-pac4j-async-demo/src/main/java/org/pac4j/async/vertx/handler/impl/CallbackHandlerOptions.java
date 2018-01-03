package org.pac4j.async.vertx.handler.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.pac4j.core.context.Pac4jConstants;

/**
 * Vert.x-style options class for a callback handler
 * @since 2.0.0
 */
@Accessors(chain=true)
public class CallbackHandlerOptions {

    @Getter @Setter
    private String defaultUrl = Pac4jConstants.DEFAULT_URL;

    @Getter @Setter
    private Boolean multiProfile = false;

    @Getter @Setter
    private Boolean renewSession = false;

}
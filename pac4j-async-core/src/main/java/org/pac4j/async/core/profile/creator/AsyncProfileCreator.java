package org.pac4j.async.core.profile.creator;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of the ProfileCreator interface. Required because OAuth profile creators make an Http call, and the
 * sync ones make this call blocking. We need to ba able to enable non-blocking i/o, which requires async behaviour.
 */
public interface AsyncProfileCreator<C extends Credentials, U extends CommonProfile> {
    /**
     * Create a profile from a credentials.
     *
     * @param credentials the given credentials
     * @param context the web context
     * @return a CompletableFuture which will complete with the profile if successful, or complete exceptionally
     * otherwise. Specific known exceptional completions: HttpAction if an additional http action is required
     */
    CompletableFuture<U> create(C credentials, AsyncWebContext context) throws HttpAction;
}

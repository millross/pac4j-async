package org.pac4j.async.core.credentials.extractor;

import org.pac4j.async.core.AsynchronousComputation;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;

import static com.aol.cyclops.invokedynamic.ExceptionSoftener.softenSupplier;

/**
 *
 */
public class BlockingCredentialsExtractorConverter {

    /**
     * Given an existing blocking but CredentialsExtractor, convert it into an async
     * one
     */
    static <C extends Credentials> AsyncCredentialsExtractor<C> fromBlockingMatcher(final CredentialsExtractor<C, WebContextBase<?>> syncExtractor, AsynchronousComputation asynchronousComputation) {
        return context -> asynchronousComputation.fromBlocking(softenSupplier(() -> syncExtractor.extract(context)));
    }

}

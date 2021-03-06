package org.pac4j.async.core.logic.authenticator;

import org.pac4j.async.core.authenticate.AsyncClientAuthenticator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.future.FutureUtils;
import org.pac4j.async.core.logic.decision.AsyncLoadProfileFromSessionDecision;
import org.pac4j.async.core.logic.decision.AsyncSaveProfileToSessionDecision;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.profile.save.AsyncProfileSaveStrategy;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 *
 */
public class AsyncDirectClientAuthenticator<U extends CommonProfile, C extends AsyncWebContext> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncDirectClientAuthenticator.class);

    private final AsyncProfileSaveStrategy saveStrategy;
    private final AsyncSaveProfileToSessionDecision saveToSessionDecision;
    private final AsyncLoadProfileFromSessionDecision loadFromSessionDecision;
    private final AsyncClientAuthenticator<U, C> perClientAuthenticator = new AsyncClientAuthenticator<>();


    public AsyncDirectClientAuthenticator(final AsyncProfileSaveStrategy saveStrategy, AsyncSaveProfileToSessionDecision saveToSessionDecision, AsyncLoadProfileFromSessionDecision loadFromSessionDecision) {
        this.saveStrategy = saveStrategy;
        this.saveToSessionDecision = saveToSessionDecision;
        this.loadFromSessionDecision = loadFromSessionDecision;
    }

    public final CompletableFuture<List<U>> authenticate(final List<AsyncClient<? extends Credentials, U>> currentClients,
                     final C context,
                     final AsyncProfileManager<U, C> manager) {

        final Stream<AsyncClient<? extends Credentials, U>> directClientsStream = currentClients.stream()
                .filter(c -> !c.isIndirect());

        final List<CompletableFuture<Supplier<CompletableFuture<Boolean>>>> saveOperationList = directClientsStream
                .peek(c -> logger.debug("Performing authentication for direct client: {}", c))
                .map(c -> {
                    final CompletableFuture<Optional<U>> profileOptionFuture = perClientAuthenticator.authenticateFor(c, context);
                    return profileOptionFuture.thenApply(profileOption -> {
                        final U profile = profileOption.orElse(null);
                        return (Supplier<CompletableFuture<Boolean>>) () -> saveStrategy.saveProfile(manager,
                                    ctx -> saveToSessionDecision.make(context, currentClients, c, profile), profile);

                    });

                }).collect(Collectors.toList());

        // We can now determine whether we successfully saved any or not and attempt to retrieve them
        return  FutureUtils.combineFuturesToList(saveOperationList)
                .thenCompose(saveStrategy::combineResults)
                .thenCompose(b -> b ? manager.getAll(loadFromSessionDecision.make(context, currentClients)) :
                        completedFuture(new LinkedList<>()));

    }

}

package org.pac4j.async.core.profile.save;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.TestCredentials;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.core.profile.CommonProfile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.pac4j.async.core.profile.save.AsyncProfileSave.MULTI_PROFILE_SAVE;
import static org.pac4j.async.core.util.TestsConstants.*;

/**
 * Tests for the single and multi profile save strategy implementations
 */
public class AsyncMultiProfileSaveTest extends VertxAsyncTestBase {

    @Test(timeout = 2000)
    public void multiProfileSaveNullProfileToEmptySession(final TestContext testContext) {
        final Async async = testContext.async();
        final AsyncProfileManager profileManager = asyncProfileManager();

        MULTI_PROFILE_SAVE.saveProfile(profileManager, p -> true, null)
                .thenCompose(checkSaveResultIs(false, profileManager))
                .<List<CommonProfile>>thenAccept(new Consumer<List<CommonProfile>>() {
                    @Override
                    public void accept(List<CommonProfile> l) {
                        assertThat(l, is(empty()));
                        async.complete();
                    }
                });
    }

    @Test(timeout = 2000)
    public void multiProfileSaveNonNullProfileToEmptySession(final TestContext testContext) {
        final TestCredentials credentials = new TestCredentials(GOOD_USERNAME, EMAIL);
        final TestProfile profile = TestProfile.from(credentials);
        profile.setClientName(TEST_CLIENT_1);

        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();
        final Async async = testContext.async();
        MULTI_PROFILE_SAVE.saveProfile(profileManager, p -> true, profile)
                .thenCompose(checkSaveResultIs(true, profileManager))
                .thenAccept(validateProfiles(async, profile));

    }

    @Test(timeout = 2000)
    public void multiProfileSaveNullProfileToNonEmptySession(final TestContext testContext) {

        final TestProfile originalProfile = profile(GOOD_USERNAME2);
        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();

        final Async async = testContext.async();
        // Save the original profile to pre-setup the session
        profileManager.save(true, originalProfile, false)
                // Then save a null profile
                .thenCompose(v -> MULTI_PROFILE_SAVE.saveProfile(profileManager, p -> true, null))
                // Check result of second save is false
                .thenCompose(checkSaveResultIs(false, profileManager))
                // And check that only original profile was stored
                .thenAccept(validateProfiles(async, originalProfile));
    }

    @Test(timeout = 2000)
    public void multiProfileSaveNonNullProfileToNonEmptySession(final TestContext testContext) {
//        throw new RuntimeException("Test not implemented");

        final TestProfile originalProfile = profile(GOOD_USERNAME2);
        originalProfile.setClientName(TEST_CLIENT_2);
        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();
        // We distinguish on client name
        final TestProfile profile = profile(GOOD_USERNAME);
        profile.setClientName(TEST_CLIENT_1);

        final Async async = testContext.async();
        profileManager.save(true, originalProfile, false)
                // Then save a null profile
                .thenCompose(v -> MULTI_PROFILE_SAVE.saveProfile(profileManager, p -> true, profile))
                // Check result of second save is true
                .thenCompose(checkSaveResultIs(true, profileManager))
                // And check that  original profile was replaced by new profile
                .thenAccept(validateProfiles(async, profile, originalProfile));
    }

    @Test(timeout = 2000)
    public void multiProfileNoProfilesSuccessfullySaved(final TestContext testContext) {
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidNotOccur(),
                () -> saveDidNotOccur());
        final Async async = testContext.async();
        MULTI_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(false));
                        async.complete();
                    });
                });
    }

    @Test
    public void multiProfileFirstProfileSucessfullySaved(final TestContext testContext) {
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidOccur(),
                () -> saveDidNotOccur());
        final Async async = testContext.async();
        MULTI_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(true));
                        async.complete();
                    });
                });
    }

    @Test
    public void multiProfileSecondProfileIsFirstToSuccessfullySave(final TestContext testContext) {
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidNotOccur(),
                () -> saveDidOccur());
        final Async async = testContext.async();
        MULTI_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(true));
                        async.complete();
                    });
                });
    }

    private CompletableFuture<Boolean> saveDidNotOccur() {
        return delayedResult(() -> false);
    }

    private CompletableFuture<Boolean> saveDidOccur() {
        return delayedResult(() -> true);
    }

    private TestProfile profile(final String userName) {
        final TestCredentials credentials = new TestCredentials(userName, EMAIL);
        return TestProfile.from(credentials);
    }

    private AsyncProfileManager asyncProfileManager() {
        final AsyncWebContext asyncWebContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
        return new AsyncProfileManager(asyncWebContext);
    }

    private Function<Boolean, CompletableFuture<List<CommonProfile>>> checkSaveResultIs(final boolean expectedResult,
                                                                                        final AsyncProfileManager profileManager) {
        return b -> {
            assertThat(b, is(expectedResult));
            return profileManager.getAll(true);

        };

    }

    private Consumer<List<CommonProfile>> validateProfiles(final Async async, final CommonProfile... expectedProfiles) {
        return l -> executionContext.runOnContext(() -> {
            assertThat(l, containsInAnyOrder(expectedProfiles));
            async.complete();
        });
    }

}
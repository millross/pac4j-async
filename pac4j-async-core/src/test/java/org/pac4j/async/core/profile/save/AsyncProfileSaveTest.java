package org.pac4j.async.core.profile.save;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.hamcrest.Matcher;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.pac4j.async.core.profile.save.AsyncProfileSave.SINGLE_PROFILE_SAVE;
import static org.pac4j.async.core.util.TestsConstants.*;

/**
 * Tests for the single and multi profile save strategy implementations
 */
public class AsyncProfileSaveTest extends VertxAsyncTestBase {

    @Test(timeout = 2000)
    public void singleProfileSaveNullProfileToEmptySession(final TestContext testContext) {
        final Async async = testContext.async();
        final AsyncProfileManager profileManager = asyncProfileManager();

        final CompletableFuture<Boolean> saveResult = SINGLE_PROFILE_SAVE.saveProfile(profileManager, p -> true, null);
        final CompletableFuture<List<CommonProfile>> profilesFuture = saveResult.thenCompose(b -> {
            assertThat(b, is(false));
            return profileManager.getAll(true);
        });
        profilesFuture.thenAccept(l -> {
            assertThat(l, is(empty()));
            async.complete();
        });
    }

    @Test(timeout = 2000)
    public void singleProfileSaveNonNullProfileToEmptySession(final TestContext testContext) {
        final TestCredentials credentials = new TestCredentials(GOOD_USERNAME, EMAIL);
        final TestProfile profile = TestProfile.from(credentials);
        profile.setClientName(TEST_CLIENT_1);

        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();
        final Async async = testContext.async();
        SINGLE_PROFILE_SAVE.saveProfile(profileManager, p -> true, profile)
                .thenCompose(checkSaveResultIs(true, profileManager))
                .thenAccept(validateProfiles(equalTo(Arrays.asList(profile)), async));

    }

    @Test(timeout = 2000)
    public void singleProfileSaveNullProfileToNonEmptySession(final TestContext testContext) {

        final TestProfile originalProfile = profile(GOOD_USERNAME2);
        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();

        final Async async = testContext.async();
        // Save the original profile to pre-setup the session
        profileManager.save(true, originalProfile, false)
                // Then save a null profile
                .thenCompose(v -> SINGLE_PROFILE_SAVE.saveProfile(profileManager, p -> true, null))
                // Check result of second save is false
                .thenCompose(checkSaveResultIs(false, profileManager))
                // And check that only original profile was stored
                .thenAccept(validateProfiles(equalTo(Arrays.asList(originalProfile)), async));
    }

    @Test(timeout = 2000)
    public void singleProfileSaveNonNullProfileToNonEmptySession(final TestContext testContext) {

        final TestProfile originalProfile = profile(GOOD_USERNAME2);
        final AsyncProfileManager<TestProfile, AsyncWebContext> profileManager = asyncProfileManager();
        final TestProfile profile = profile(GOOD_USERNAME);

        final Async async = testContext.async();
        profileManager.save(true, originalProfile, false)
                // Then save a null profile
                .thenCompose(v -> SINGLE_PROFILE_SAVE.saveProfile(profileManager, p -> true, profile))
                // Check result of second save is true
                .thenCompose(checkSaveResultIs(true, profileManager))
                // And check that  original profile was replaced by new profile
                .thenAccept(validateProfiles(equalTo(Arrays.asList(profile)), async));
    }

    @Test(timeout = 2000)
    public void singleProfileNoProfilesSuccessfullySaved(final TestContext testContext) {
        final CompletableFuture<Boolean> saveDidNotOccur = delayedResult(() -> false);
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidNotOccur,
                () -> saveDidNotOccur);
        final Async async = testContext.async();
        SINGLE_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(false));
                        async.complete();
                    });
                });
    }

    @Test
    public void singleProfileFirstProfileSucessfullySaved(final TestContext testContext) {
        final CompletableFuture<Boolean> saveDidNotOccur = delayedResult(() -> false);
        final CompletableFuture<Boolean> saveDidOccur = delayedResult(() -> true);
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidOccur,
                () -> saveDidNotOccur);
        final Async async = testContext.async();
        SINGLE_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(true));
                        async.complete();
                    });
                });
    }

    @Test
    public void singleProfileSecondProfileIsFirstToSuccessfullySave(final TestContext testContext) {
        final CompletableFuture<Boolean> saveDidNotOccur = delayedResult(() -> false);
        final CompletableFuture<Boolean> saveDidOccur = delayedResult(() -> true);
        final List<Supplier<CompletableFuture<Boolean>>> saveSimulations = Arrays.asList(() -> saveDidNotOccur,
                () -> saveDidOccur);
        final Async async = testContext.async();
        SINGLE_PROFILE_SAVE.combineResults(saveSimulations)
                .thenAccept(b -> {
                    executionContext.runOnContext(() -> {
                        assertThat(b, is(true));
                        async.complete();
                    });
                });
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

    private Consumer<List<CommonProfile>> validateProfiles(final Matcher<List<CommonProfile>> expectedProfiles, final Async async) {
        return l -> {
            assertThat(l, is(expectedProfiles));
            async.complete();
        };
    }

}
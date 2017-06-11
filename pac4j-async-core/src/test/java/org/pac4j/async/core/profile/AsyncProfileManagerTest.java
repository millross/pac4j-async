package org.pac4j.async.core.profile;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.MockAsyncWebContextBuilder;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.pac4j.core.context.Pac4jConstants.USER_PROFILES;

/**
 * Test for key async profile manager behaviours
 */
public class AsyncProfileManagerTest extends VertxAsyncTestBase{

    private final static String CLIENT1 = "client1";
    private final static String CLIENT2 = "client2";
    private final static CommonProfile PROFILE1 = new CommonProfile();
    private final static CommonProfile PROFILE2 = new CommonProfile();
    private final static CommonProfile PROFILE3 = new CommonProfile();

    private LinkedHashMap<String, CommonProfile> profiles;
    private AsyncWebContext webContext;
    private AsyncProfileManager<CommonProfile, AsyncWebContext> profileManager;

    static {
        PROFILE1.setId("ID1");
        PROFILE1.setClientName(CLIENT1);
        PROFILE2.setId("ID2");
        PROFILE2.setClientName(CLIENT2);
        PROFILE3.setId("ID3");
        PROFILE3.setClientName(CLIENT1);
    }

    @Before
    public void setUp() {
        webContext = MockAsyncWebContextBuilder.from(rule.vertx(), executionContext).build();
        // Use the test's execution context for async work here - this compares with the fact that  it is
        // effectively the execution context in force when the web context is created
        profileManager = new AsyncProfileManager(webContext);
        profiles = new LinkedHashMap<>();
    }

    @Test(timeout = 1000)
    public void testGetNullProfile(final TestContext testContext) {
        executeAsyncGetTest(testContext, true, assertNoProfileReturned());
    }

    @Test(timeout = 1000)
    public void testGetNoProfile(final TestContext testContext) {
        webContext.setSessionAttribute(USER_PROFILES, profiles)
               .thenRun(() -> executeAsyncGetTest(testContext, true, assertNoProfileReturned()));
    }

    @Test(timeout = 1000)
    public void testGetOneProfileFromSession(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() -> executeAsyncGetTest(testContext, true,
                        assertAuthenticatedWithProfile(PROFILE1)));

    }

    @Test(timeout = 1000)
    public void testGetOneProfilesFromSessionFirstOneAnonymous(final TestContext testContext) {

        profiles.put("first", new AnonymousProfile());
        profiles.put(CLIENT1, PROFILE1);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
            .thenRun(() -> executeAsyncGetTest(testContext, true,
                assertAuthenticatedWithProfile(PROFILE1)));
    }

    @Test(timeout = 1000)
    public void testGetOneTwoProfilesFromSession(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        profiles.put(CLIENT2, PROFILE2);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetTest(testContext, true,
                assertAuthenticatedWithProfile(PROFILE1)));
    }

    @Test(timeout = 1000)
    public void testGetOneProfileFromRequest(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetTest(testContext, false,
                assertNoProfileReturned()));
    }

    @Test(timeout = 1000)
    public void testGetAllNullProfile(final TestContext testContext) {

        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetAllTest(testContext, true,
                assertNotAuthenticatedAndEmptyProfileListReturned()));

    }

    @Test(timeout = 1000)
    public void testGetAllNoProfile(final TestContext testContext) {
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetAllTest(testContext, true,
                assertNotAuthenticatedAndEmptyProfileListReturned()));

    }

    @Test(timeout = 1000)
    public void testGetAllOneProfileFromSession(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetAllTest(testContext, true,
                assertProfileListReturned(PROFILE1)));
    }

    @Test(timeout = 1000)
    public void testGetAllTwoProfilesFromSession(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        profiles.put(CLIENT2, PROFILE2);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetAllTest(testContext, true,
                assertProfileListReturned(PROFILE1, PROFILE2)));
    }

    @Test(timeout = 1000)
    public void testGetAllTwoProfilesFromSessionAndRequest(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        webContext.setRequestAttribute(USER_PROFILES, profiles);
        final LinkedHashMap<String, CommonProfile> profiles2 = new LinkedHashMap<>();
        profiles2.put(CLIENT2, PROFILE2);
        webContext.setSessionAttribute(USER_PROFILES, profiles2)
                .thenRun(() ->executeAsyncGetAllTest(testContext, true,
                assertProfileListReturned(PROFILE1, PROFILE2)));
    }

    @Test(timeout = 1000)
    public void testGetAllOneProfileFromRequest(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);
        webContext.setSessionAttribute(USER_PROFILES, profiles)
                .thenRun(() ->executeAsyncGetAllTest(testContext, false,
                assertEmptyProfileListReturned()));
    }

    @Test(timeout = 1000)
    public void testRemoveSessionFalse(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);

        testProfileInSessionAfter(webContext.setSessionAttribute(USER_PROFILES, profiles),
                v -> profileManager.remove(false),
                assertAuthenticatedWithProfile(PROFILE1),
                testContext);
    }

    @Test(timeout = 1000)
    public void testRemoveSessionTrue(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);

        testProfileInSessionAfter(webContext.setSessionAttribute(USER_PROFILES, profiles),
                v -> profileManager.remove(true),
                assertNoProfileReturned(),
                testContext);
    }

    @Test(timeout = 1000)
    public void testLogoutSession(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);

        testProfileInSessionAfter(webContext.setSessionAttribute(USER_PROFILES, profiles),
                v -> profileManager.logout(),
                assertNoProfileReturned(),
                testContext);

    }

    @Test(timeout = 1000)
    public void testRemoveRequestFalse(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);
        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfileInSessionAfter(completedFuture(null),
                v -> profileManager.remove(false),
                assertNoProfileReturned(),
                testContext);
    }

    @Test(timeout = 1000)
    public void testRemoveRequestTrue(final TestContext testContext) {
        profiles.put(CLIENT1, PROFILE1);

        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfileInSessionAfter(completedFuture(null),
                v -> profileManager.remove(true),
                assertNoProfileReturned(),
                testContext);
    }

    @Test(timeout = 1000)
    public void saveOneProfileNoMulti(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);

        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfilesInSessionAfter(profileManager.save(true, PROFILE2, false),
                v -> completedFuture(null),
                assertProfileListReturned(PROFILE2),
                testContext);

    }

    @Test(timeout = 1000)
    public void saveTwoProfilesNoMulti(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);
        profiles.put(CLIENT2, PROFILE2);

        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfilesInSessionAfter(profileManager.save(true, PROFILE3, false),
                v -> completedFuture(null),
                assertProfileListReturned(PROFILE3),
                testContext);

    }

    @Test(timeout = 1000)
    public void saveOneProfileMulti(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);

        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfilesInSessionAfter(profileManager.save(true, PROFILE2, true),
                v -> completedFuture(null),
                assertProfileListReturned(PROFILE1, PROFILE2),
                testContext);

    }

    @Test(timeout = 1000)
    public void saveTwoProfilesMulti(final TestContext testContext) {

        profiles.put(CLIENT1, PROFILE1);
        profiles.put(CLIENT2, PROFILE2);

        webContext.setRequestAttribute(USER_PROFILES, profiles);

        testProfilesInSessionAfter(profileManager.save(true, PROFILE3, true),
                v -> completedFuture(null),
                assertProfileListReturned(PROFILE2, PROFILE3),
                testContext);

        profileManager.save(true, PROFILE3, true);
    }

    @Test(timeout = 1000)
    public void testSingleProfileFromSessionDirectly(final TestContext testContext) {

        final CommonProfile profile = new CommonProfile();
        profile.setClientName(CLIENT1);

        testProfilesInSessionAfter(webContext.setSessionAttribute(USER_PROFILES, profile),
                v -> completedFuture(null),
                assertProfileListReturned(profile),
                testContext);
    }

    @Test(timeout = 1000)
    public void testSingleProfileFromRequestDirectly(final TestContext testContext) {
        final CommonProfile profile = new CommonProfile();
        profile.setClientName(CLIENT1);

        webContext.setRequestAttribute(USER_PROFILES, profile);

        final Async async = testContext.async();

        profileManager.getAll(false)
                .thenAccept(l -> executionContext.runOnContext(() -> {
                    assertThat(l, is(Arrays.asList(profile)));
                    async.complete();
                }));
    }

    @Test(timeout = 1000)
    public void testIsAuthenticatedAnonymousProfile(final TestContext testContext) {

        profiles.put(CLIENT1, AnonymousProfile.INSTANCE);

        final Async async = testContext.async();

        profileManager.isAuthenticated()
                .thenAccept(b -> executionContext.runOnContext(() -> {
                    assertThat(b, is(false));
                    async.complete();
                }));

    }

    private <T> void testProfileInSessionAfter(final CompletableFuture<T> initialSetupFuture,
                                               final Function<T, CompletableFuture<Void>> operation,
                                               final BiConsumer<CompletableFuture<Optional<CommonProfile>>, Async> assertions,
                                               final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<Optional<CommonProfile>> profilesInSessionFuture = initialSetupFuture
                .thenCompose(operation)
                .thenCompose(v -> profileManager.get(true));
        assertions.accept(profilesInSessionFuture, async);

    }

    private <T> void testProfilesInSessionAfter(final CompletableFuture<T> initialSetupFuture,
                                               final Function<T, CompletableFuture<Void>> operation,
                                               final BiConsumer<CompletableFuture<List<CommonProfile>>, Async> assertions,
                                               final TestContext testContext) {
        final Async async = testContext.async();
        final CompletableFuture<List<CommonProfile>> profilesInSessionFuture = initialSetupFuture
                .thenCompose(operation)
                .thenCompose(v -> profileManager.getAll(true));
        assertions.accept(profilesInSessionFuture, async);

    }

    private BiConsumer<CompletableFuture<Optional<CommonProfile>>, Async> assertNoProfileReturned() {
        return (f, async) -> f.thenAccept(o -> executionContext.runOnContext(() -> {
            assertThat(o.isPresent(), is(false));
            async.complete();
        }));
    }

    private BiConsumer<CompletableFuture<List<CommonProfile>>, Async> assertNotAuthenticatedAndEmptyProfileListReturned() {
        return (f, async) -> f.thenCompose(l -> {
            executionContext.runOnContext(() -> assertThat(l, is(empty())));
            return profileManager.isAuthenticated();
        }).thenAccept(b -> {
            assertThat(b, is(false));
            async.complete();
        });
    }

    private BiConsumer<CompletableFuture<List<CommonProfile>>, Async> assertEmptyProfileListReturned() {
        return assertProfileListReturned();
    }

    private BiConsumer<CompletableFuture<List<CommonProfile>>, Async> assertProfileListReturned(CommonProfile... profiles) {
        return (f, async) -> f.thenAccept(l -> executionContext.runOnContext(() -> {
            assertThat(l, is(Arrays.asList(profiles)));
            async.complete();
        }));
    }

    private BiConsumer<CompletableFuture<Optional<CommonProfile>>, Async> assertAuthenticatedWithProfile(final CommonProfile profile) {
        return (f, async) -> f.thenCompose(o -> {
            executionContext.runOnContext(() -> assertThat(o.get(), is(profile)));
            return profileManager.isAuthenticated();
        }).thenAccept(b -> executionContext.runOnContext(() -> {
            assertThat(b, is(true));
            async.complete();
        }));
    }

    private void executeAsyncGetTest(final TestContext testContext, final boolean fromSession,
                                     final BiConsumer<CompletableFuture<Optional<CommonProfile>>, Async> asyncAssertions) {

        final Async async = testContext.async();

        final CompletableFuture<Optional<CommonProfile>> future = profileManager.get(fromSession);

        asyncAssertions.accept(future, async);

    }

    private void executeAsyncGetAllTest(final TestContext testContext, final boolean fromSession,
                                        final BiConsumer<CompletableFuture<List<CommonProfile>>, Async> asyncAssertions) {
        final Async async = testContext.async();

        final CompletableFuture<List<CommonProfile>> future = profileManager.getAll(fromSession);

        asyncAssertions.accept(future, async);
    }

}
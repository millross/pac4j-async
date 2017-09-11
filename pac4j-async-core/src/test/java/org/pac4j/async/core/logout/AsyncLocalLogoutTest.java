package org.pac4j.async.core.logout;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.async.core.TestProfile;
import org.pac4j.async.core.VertxAsyncTestBase;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.destruction.AsyncSessionDestructionStrategy;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.pac4j.async.core.util.TestsConstants.TEST_CREDENTIALS;

/**
 * Tests for the AsyncLocalLogout enum members provided by default
 */
public class AsyncLocalLogoutTest extends VertxAsyncTestBase {

    private static final TestProfile TEST_PROFILE =TestProfile.from(TEST_CREDENTIALS);

    final AsyncSessionDestructionStrategy sessionDestructionStrategy = mock(AsyncSessionDestructionStrategy.class);
    final AsyncWebContext asyncWebContext = mock(AsyncWebContext.class);
    final AsyncProfileManager<CommonProfile, AsyncWebContext> profileManager = mock(AsyncProfileManager.class);

    @Before
    public void setUp() {
        when(sessionDestructionStrategy.attemptSessionDestructionFor(asyncWebContext))
                .thenReturn(completedFuture(null));
        when(profileManager.logout()).thenReturn(completedFuture(null));
    }

    @Test
    public void testAlwaysLogoutWithProfile(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        when(profileManager.getAll(anyBoolean())).thenReturn(completedFuture(Arrays.asList(TEST_PROFILE)));
        assertSuccessfulEvaluation(AsyncLocalLogout.ALWAYS_LOGOUT.logout(profileManager, sessionDestructionStrategy, asyncWebContext),
                profiles -> {
                    assertThat(profiles, containsInAnyOrder(TestProfile.from(TEST_CREDENTIALS)));
                    verify(profileManager, times(1)).logout();
                    verify(sessionDestructionStrategy, times(1)).attemptSessionDestructionFor(any(AsyncWebContext.class));
                }, async);
    }

    @Test
    public void testAlwaysLogoutWithNoProfiles(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        when(profileManager.getAll(anyBoolean())).thenReturn(completedFuture(new ArrayList<>()));
        assertSuccessfulEvaluation(AsyncLocalLogout.ALWAYS_LOGOUT.logout(profileManager, sessionDestructionStrategy, asyncWebContext),
                profiles -> {
                    assertThat(profiles, is(empty()));
                    verify(profileManager, times(1)).logout();
                    verify(sessionDestructionStrategy, times(1)).attemptSessionDestructionFor(any(AsyncWebContext.class));
                }, async);
    }

    @Test
    public void testProfileDependentWithOneProfiles(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        when(profileManager.getAll(anyBoolean())).thenReturn(completedFuture(Arrays.asList(TEST_PROFILE)));
        assertSuccessfulEvaluation(AsyncLocalLogout.PROFILE_PRESENCE_DEPENDENT.logout(profileManager, sessionDestructionStrategy, asyncWebContext),
                profiles -> {
                    assertThat(profiles, containsInAnyOrder(TestProfile.from(TEST_CREDENTIALS)));
                    verify(profileManager, never()).logout();
                    verify(sessionDestructionStrategy, never()).attemptSessionDestructionFor(any(AsyncWebContext.class));
                }, async);
    }

    @Test
    public void testProfileDependentWithTwoProfiles(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        when(profileManager.getAll(anyBoolean())).thenReturn(completedFuture(Arrays.asList(TEST_PROFILE, TEST_PROFILE)));
        assertSuccessfulEvaluation(AsyncLocalLogout.PROFILE_PRESENCE_DEPENDENT.logout(profileManager, sessionDestructionStrategy, asyncWebContext),
                profiles -> {
                    assertThat(profiles, containsInAnyOrder(TestProfile.from(TEST_CREDENTIALS), TestProfile.from(TEST_CREDENTIALS)));
                    verify(profileManager, times(1)).logout();
                    verify(sessionDestructionStrategy, times(1)).attemptSessionDestructionFor(any(AsyncWebContext.class));
                }, async);
    }

    @Test
    public void testProfileDependentWithNoProfiles(final TestContext testContext) throws Exception {
        final Async async = testContext.async();
        when(profileManager.getAll(anyBoolean())).thenReturn(completedFuture(new ArrayList<>()));
        assertSuccessfulEvaluation(AsyncLocalLogout.PROFILE_PRESENCE_DEPENDENT.logout(profileManager, sessionDestructionStrategy, asyncWebContext),
                profiles -> {
                    assertThat(profiles, is(empty()));
                    verify(profileManager, never()).logout();
                    verify(sessionDestructionStrategy, never()).attemptSessionDestructionFor(any(AsyncWebContext.class));
                }, async);
    }

}
package org.pac4j.async.core.matching;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.util.CommonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.pac4j.async.core.future.FutureUtils.shortCircuitedFuture;

/**
 *
 */
public class DefaultAsyncMatchingChecker implements AsyncMatchingChecker {
    @Override
    public CompletableFuture<Boolean> matches(AsyncWebContext context, String matcherNames, Map<String, AsyncMatcher> matchersMap) {

        // if we have a matcher name (which may be a list of matchers names)
        if (CommonHelper.isNotBlank(matcherNames)) {

            // we must have matchers
            CommonHelper.assertNotNull("matchersMap", matchersMap);
            final String[] names = matcherNames.split(Pac4jConstants.ELEMENT_SEPRATOR);

            final List<AsyncMatcher> matchers = Arrays.stream(names)
                    .map(n -> matchersMap.entrySet().stream()
                            .filter(e -> CommonHelper.areEqualsIgnoreCaseAndTrim(e.getKey(), n))
                            .peek(e -> CommonHelper.assertNotNull("matchersMap['" + n + "']", e))
                            .findFirst().map(e -> e.getValue()).orElse(null))
                    .collect(Collectors.toList());

            return shortCircuitedFuture(matchers.stream()
                        .map(m -> () -> m.matches(context)));
        }

        return CompletableFuture.completedFuture(true);
    }

}

package com.exasol.matcher;

import java.util.Arrays;

import org.hamcrest.*;

/**
 * This Hamcrest matcher improves upon the error reporting of the {@link org.hamcrest.core.AllOf} matcher.
 *
 * @param <T> matcher type
 */
public class EnhancedAllOfMatcher<T> extends DiagnosingMatcher<T> {
    private final Iterable<Matcher<? super T>> matchers;

    private EnhancedAllOfMatcher(final Iterable<Matcher<? super T>> matchers) {
        this.matchers = matchers;
    }

    /**
     * Get an {@link EnhancedAllOfMatcher} for multiple other matchers.
     *
     * @param matchers matchers to combine
     * @param <T>      matcher type
     * @return built {@link EnhancedAllOfMatcher}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Matcher<T> enhancedAllOf(final Matcher<? super T>... matchers) {
        return new EnhancedAllOfMatcher<T>(Arrays.asList(matchers));
    }

    @Override
    protected boolean matches(final Object item, final Description mismatchDescription) {
        for (final Matcher<? super T> matcher : this.matchers) {
            if (!matcher.matches(item)) {
                matcher.describeMismatch(item, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendList("(", " and ", ")", this.matchers);
    }
}

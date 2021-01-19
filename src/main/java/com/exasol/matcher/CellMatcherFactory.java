package com.exasol.matcher;

import static com.exasol.matcher.EnhancedAllOfMatcher.enhancedAllOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import org.hamcrest.Matcher;

/**
 * This class builds Hamcrest matchers for given cell values.
 */
public class CellMatcherFactory {
    private CellMatcherFactory() {
        // empty on purpose
    }

    /**
     * Build a Hamcrest matcher that matches values if they are equal to the given value.
     * 
     * @param expectedCellValue expected value / matcher
     * @param typeMatchMode     type match mode
     * @param fuzzyTolerance    tolerance for fuzzy matching numbers
     * @return built matcher
     */
    public static Matcher<Object> cellMatcher(final Object expectedCellValue, final TypeMatchMode typeMatchMode,
            final BigDecimal fuzzyTolerance) {
        if (expectedCellValue == null) {
            return nullValue();
        } else {
            final FuzzyCellMatcher<Object> valueMatcher = FuzzyCellMatcher.fuzzilyEqualTo(expectedCellValue,
                    fuzzyTolerance);
            return buildTypeMatcher(expectedCellValue, typeMatchMode, valueMatcher);
        }
    }

    private static Matcher<Object> buildTypeMatcher(final Object expectedCellValue, final TypeMatchMode typeMatchMode,
            final FuzzyCellMatcher<Object> valueMatcher) {
        switch (typeMatchMode) {
        case STRICT:
            return enhancedAllOf(instanceOf(expectedCellValue.getClass()), valueMatcher);
        case UPCAST_ONLY:
            return enhancedAllOf(UpcastOnlyCellMatcher.isOnlyUpcastTo(expectedCellValue), valueMatcher);
        case NO_JAVA_TYPE_CHECK:
        default:
            return valueMatcher;
        }
    }
}

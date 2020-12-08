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
        // empty ojn purpose
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
            if (typeMatchMode.equals(TypeMatchMode.STRICT)) {
                return enhancedAllOf(instanceOf(expectedCellValue.getClass()), valueMatcher);
            } else if (typeMatchMode.equals(TypeMatchMode.UPCAST_ONLY)) {
                return enhancedAllOf(UpcastOnlyCellMatcher.isOnlyUpcastTo(expectedCellValue), valueMatcher);
            } else {
                return valueMatcher;
            }
        }
    }
}

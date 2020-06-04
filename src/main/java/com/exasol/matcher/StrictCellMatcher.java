package com.exasol.matcher;

/**
 * Matcher that matches cell value and type strictly.
 */
public class StrictCellMatcher implements CellMatcher {
    @Override
    public boolean match(final Object actual, final Object expected) {
        return actual.equals(expected);
    }
}
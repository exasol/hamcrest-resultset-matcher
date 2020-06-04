package com.exasol.matcher;

/**
 * Matcher for cell values
 */
interface CellMatcher {
    /**
     * Match two cells.
     *
     * @param actual   actual cell content and type.
     * @param expected expected cell content and type.
     * @return true if the two cells match
     */
    public boolean match(final Object actual, final Object expected);
}
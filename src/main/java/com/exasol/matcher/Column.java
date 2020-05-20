package com.exasol.matcher;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

/**
 * Description of a column in a JDBC result set.
 */
public class Column implements SelfDescribing {
    private static final String ANY_TYPE = "*";
    private final String typeName;

    private Column(final String typeName) {
        this.typeName = typeName;
    }

    static Column any() {
        return new Column(ANY_TYPE);
    }

    static Column column(final String typeName) {
        return new Column(typeName);
    }

    public String getTypeName() {
        return this.typeName;
    }

    public boolean hasType() {
        return !ANY_TYPE.equals(this.typeName);
    }

    public boolean isSpecified() {
        return hasType();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(this.typeName);
    }
}
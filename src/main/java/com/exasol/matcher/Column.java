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

    /**
     * Create a new instance of a column description where the type is not validated.
     *
     * @return new instance
     */
    public static Column any() {
        return new Column(ANY_TYPE);
    }

    /**
     * Create a new instance of a column description where the type is not validated.
     *
     * @param typeName name of the type that is checked against the column's type
     *
     * @return new instance
     */
    public static Column column(final String typeName) {
        return new Column(typeName);
    }

    /**
     * Get the name of the column's data type.
     *
     * @return data type name
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Check whether a data type is specified.
     *
     * @return {@code true} if a data type is given
     */
    public boolean hasType() {
        return !ANY_TYPE.equals(this.typeName);
    }

    /**
     * Check if any validation criteria are specified.
     * 
     * @return {@code true} if any validation criteria are specified.
     */
    public boolean isSpecified() {
        return hasType();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(this.typeName);
    }
}
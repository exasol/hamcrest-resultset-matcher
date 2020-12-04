package com.exasol.matcher;

/**
 * Modes for fuzzy matching.
 */
public enum TypeMatchMode {
    /**
     * In this mode, this matcher does not check if the Java data type of the expect and the actual value match. It only
     * compares the value.
     */
    NO_TYPE_CHECK,
    /**
     * In this mode this matcher checks that the actual type can be safely casted to the expected type. If for example
     * the result contains an Short but an Integer is expected the matcher would match. The other way around (Integer to
     * Short) it would not.
     */
    UPCAST_ONLY,
    /**
     * In this mode checks that the data types exactly match.
     */
    STRICT
}

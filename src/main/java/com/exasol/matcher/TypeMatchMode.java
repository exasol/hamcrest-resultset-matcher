package com.exasol.matcher;

/**
 * Modes for result set matching.
 */
public enum TypeMatchMode {
    /**
     * In this mode, this matcher does not check if the Java data type of the expect and the actual value match. It only
     * compares the value.
     */
    NO_JAVA_TYPE_CHECK,
    /**
     * In this mode this matcher checks that the actual type can be safely casted to the expected type. If for example
     * the result contains a short but an integer is expected the result matches. The other way around (integer to
     * short) it does not.
     */
    UPCAST_ONLY,
    /**
     * In this mode Java data types must be an exact match.
     */
    STRICT
}

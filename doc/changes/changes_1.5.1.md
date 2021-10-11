# Matcher for SQL Result Sets 1.5.1, released 2021-10-12

Code name: Null-Value Matching

## Summary

Version 1.5.1 of the `hamcrest-result-matcher` fixes value matching in case the actual value was a `null` value.

## Features

* #37: Fix matching of `null` values

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:5.0.0` to `5.1.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:0.6.0` to `0.7.0`

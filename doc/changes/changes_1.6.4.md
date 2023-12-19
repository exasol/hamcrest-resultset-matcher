# Matcher for SQL Result Sets 1.6.4, released 2023-12-20

Code name: Upgrade dependencies on top of 1.6.3

## Summary

This release updates dependencies compared to version 1.6.3.

Please note that vulnerability CVE-2022-46337 in test dependency `org.apache.derby:derby` is ignored because fixed versions are not available for Java 11.

## Security

* #51: Updated dependencies

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.3` to `7.0.0`
* Updated `org.testcontainers:jdbc:1.19.2` to `1.19.3`
* Updated `org.testcontainers:junit-jupiter:1.19.2` to `1.19.3`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.16` to `3.0.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.2` to `3.2.3`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.6.2` to `3.6.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.2` to `3.2.3`
* Added `org.apache.maven.plugins:maven-toolchains-plugin:3.1.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.1` to `2.16.2`

# Matcher for SQL Result Sets 1.7.3, released 2026-07-27

Code name: Fixes for vulnerability CVE-2025-48924

## Summary

While we still compile with Java 11, the integration tests in this project now only run with Java 21 and later. This prevents using outdated versions of the test dependency Apache Derby. From a user perspective nothing changes. This just makes builds safe.

Exasol users do not need to update.

This release fixes the following transitive test scope vulnerabilities:

### CVE-2025-48924 (CWE-674) in dependency `org.apache.commons:commons-lang3:jar:3.16.0:test`

Uncontrolled Recursion vulnerability in Apache Commons Lang.

This issue affects Apache Commons Lang: Starting with commons-lang:commons-lang 2.0 to 2.6, and, from org.apache.commons:commons-lang3 3.0 before 3.18.0.

The methods ClassUtils.getClass(...) can throw StackOverflowError on very long inputs. Because an Error is usually not handled by applications and libraries, a StackOverflowError could cause an application to stop.

CVE: CVE-2025-48924
CWE: CWE-674

### CVE-2026-9563 (CWE-400) in dependency `org.eclipse.parsson:parsson:jar:1.1.7:runtime`
In Eclipse Parsson published Maven Central artifacts before version 1.1.8, the JSON parser did not enforce a default maximum on the number of characters consumed while parsing a single JSON document. Applications that parse attacker- controlled JSON can be forced to consume excessive CPU and memory by processing very large documents, including large arrays, objects, strings, numbers, whitespace, or nested structures, resulting in a denial of service. Eclipse Parsson 1.1.8 introduces a configurable maximum parsing limit with a default limit of 15 million parser-consumed characters.

#### References
* https://guide.sonatype.com/vulnerability/CVE-2026-9563?component-type=maven&component-name=org.eclipse.parsson%2Fparsson&utm_source=ossindex-client&utm_medium=integration&utm_content=1.8.1
* http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2026-9563
* https://github.com/eclipse-ee4j/parsson/pull/169
* https://gitlab.eclipse.org/security/vulnerability-reports/-/work_items/444

#### References

- https://ossindex.sonatype.org/vulnerability/CVE-2025-48924?component-type=maven&component-name=org.apache.commons%2Fcommons-lang3&utm_source=ossindex-client&utm_medium=integration&utm_content=1.8.1
- http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2025-48924
- https://github.com/advisories/GHSA-j288-q9x7-2f5v

## Security

* #64: Fixed vulnerabilities CVE-2025-48924, CVE-2026-9563 by updating test-scoped dependencies

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.1.7` to `8.0.1`
* Updated `org.apache.derby:derby:10.15.2.0` to `10.17.1.0`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.11.0` to `5.14.4`
* Updated `org.junit.jupiter:junit-jupiter-params:5.11.0` to `5.14.4`
* Updated `org.slf4j:slf4j-jdk14:2.0.17` to `2.0.18`
* Removed `org.testcontainers:jdbc:1.20.1`
* Updated `org.testcontainers:junit-jupiter:1.20.1` to `1.21.4`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.4` to `2.1.0`
* Updated `com.exasol:project-keeper-maven-plugin:5.2.3` to `5.7.4`
* Removed `com.exasol:quality-summarizer-maven-plugin:0.2.0`
* Updated `io.github.git-commit-id:git-commit-id-maven-plugin:9.0.1` to `10.0.0`
* Updated `org.apache.maven.plugins:maven-artifact-plugin:3.6.0` to `3.6.1`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.14.0` to `3.15.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.5.0` to `3.6.3`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.5.3` to `3.5.6`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.2.7` to `3.2.8`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.11.2` to `3.12.0`
* Updated `org.apache.maven.plugins:maven-resources-plugin:3.3.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.21.0` to `3.22.0`
* Updated `org.apache.maven.plugins:maven-source-plugin:3.2.1` to `3.4.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.5.3` to `3.5.6`
* Added `org.codehaus.mojo:build-helper-maven-plugin:3.6.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.7.0` to `1.7.3`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.18.0` to `2.21.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.13` to `0.8.15`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751` to `5.7.0.6970`
* Updated `org.sonatype.central:central-publishing-maven-plugin:0.7.0` to `0.11.0`
* Added `org.spdx:spdx-maven-plugin:1.0.4`

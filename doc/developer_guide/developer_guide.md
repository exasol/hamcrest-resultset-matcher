# Developer Guide

## Building with Java 21 and Java 11 compatibility

The build uses Java 21 as its primary JDK. Maven selects that JDK through the
`java.version` property in `pom.xml` and the Maven Toolchains configuration.
Run the complete build, including integration tests, with:

```shell
mvn clean verify
```

The published library remains compatible with Java 11. The Maven Compiler and
Javadoc plugins use `--release 11` (`<release>11</release>`), which both emits
Java 11 bytecode and limits the available Java APIs to the Java 11 API surface.
Consequently, production code must not use Java language features or JDK APIs
introduced after Java 11, even though the build runs on Java 21.

The CI build also runs the regular unit-test selection with Java 11 to validate
that compatibility:

```shell
mvn --batch-mode clean test \
    -Djava.version=11 \
    -Denforcer.skip=true
```

`-Denforcer.skip=true` is required because the normal build requires Java 21.
The `test` goal runs Maven Surefire only; use `verify` on Java 21 to include
the Maven Failsafe integration tests.

## Marking tests that require Java 21

Apache Derby 10.17 requires Java 21 or later. Mark a Derby-backed test class or
method with JUnit Jupiter's JRE-range condition so that JUnit skips it on an
older runtime:

```java
import static org.junit.jupiter.api.condition.JRE.JAVA_21;

import org.junit.jupiter.api.condition.EnabledForJreRange;

@EnabledForJreRange(min = JAVA_21)
class DatabaseBackedTest {
    // ...
}
```

Use the annotation rather than a custom JUnit tag. It expresses the runtime
requirement at the test itself and applies to both individual methods and whole
test classes.

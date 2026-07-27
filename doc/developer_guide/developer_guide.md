# Developer Guide

## Testing with multiple Java versions

The production sources are compiled as Java 11-compatible bytecode. The full test suite uses newer Java versions because Apache Derby 10.17 requires Java 21 or later.

### Java 11 unit tests

The Java 11 CI step runs the normal Maven Surefire test selection and excludes tests tagged `requires-java-21`:

```shell
mvn --batch-mode clean test \
    -Djava.version=11 \
    -Dtest.excludeTags=requires-java-21 \
    -Denforcer.skip=true
```

`-Denforcer.skip=true` is required because the regular build enforces the primary Java version. The command runs Surefire tests only; it does not invoke Maven Failsafe, so integration tests are not run with Java 11.

### Marking tests that need Java 21

Add the `requires-java-21` JUnit tag to every Surefire test class that uses Derby or another dependency which cannot run on Java 11:

```java
import org.junit.jupiter.api.Tag;

@Tag("requires-java-21")
class DatabaseBackedTest {
    // ...
}
```

Do not add class names to the CI command. The tag keeps the Java 11 test selection automatic as tests are added or renamed.

# Marcel Maven Plugin

This project allows to compile Marcel source code from a Maven project.

## How to use

Add the plugin in your pom.

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>com.tambapps.marcel.maven</groupId>
        <artifactId>marcel-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
                <goal>compileTests</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Then it will compile all Marcel source files under the `src/main/marcel` directory

You can consult the [basicCompile example](./examples/basicCompile/) to get a full working example.

### Cross compilation

You can have a project with Java AND Marcel sources. Put the Java source files under the `src/main/java` folder,
and Marcel files under `src/main/marcel`.

Note that you **can** use/reference Java source files from Marcel source files, but you **cannot** reference Marcel files from Java's.
To use cross compilation, you'll also need a plugin to compile Java files, like in the below example

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>com.tambapps.marcel.maven</groupId>
        <artifactId>marcel-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
              <goal>compileTests</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
```

You can consult the [crossCompile example](./examples/crossCompile/) to get a full working example.

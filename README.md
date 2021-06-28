## SBT-Delegate-Maven-Plugin

A Maven plugin that caches & executes SBT. It is helpful when:

* Your work's CI only supports Maven.
* You'd like to use a SBT plugin or feature not supported in Maven, and vice versa.
* You'd like to use [Scala.js](https://www.scala-js.org/) or [build for multiple platforms](https://github.com/portable-scala/sbt-crossproject) while keeping a Maven build definition.

## Similar Plugins
* [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin)
* [scalor-maven-plugin](https://github.com/random-maven/scalor-maven-plugin)
* [sbt-compiler-maven-plugin](https://github.com/sbt-compiler-maven-plugin/sbt-compiler-maven-plugin)


```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.augustnagro</groupId>
      <artifactId>sbt-delegate-maven-plugin</artifactId>
      <version>0.1.0</version>
    </plugin>
  </plugins>
</build>
```

## Usage

This plugin is extremely simple. It extracts the included SBT zip file to a temporary directory, and then runs SBT using the Maven configuration. Future runs re-use the extracted SBT.

First, create a regular SBT project with a build.sbt file. For example, with `sbt new scala/scala3.g8`.

Next, create a `pom.xml` file with the plugin:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>test-project</artifactId>
  <version>0.1.0-SNAPSHOT</version>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>com.augustnagro</groupId>
        <artifactId>sbt-delegate-maven-plugin</artifactId>
        <version>0.1.0</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>sbt</goal>
            </goals>
            <configuration>
              <jvmParams>
                <param>-DsomeJavaProperty=true</param>
              </jvmParams>
              <sbtParams>
                <param>clean</param>
                <param>test</param>
                <param>package</param>
              </sbtParams>
            </configuration>
          </execution>
        </executions>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
          <skipMain>true</skipMain>
          <skip>true</skip>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-install-plugin</artifactId>
        <version>2.5.2</version>
        <configuration>
          <skip>true</skip>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

There is only one goal, `sbt`, and two configuration parameters available. `jvmParams` is an array of parameters for the JVM running SBT, and `sbtParams` are the parameters passed to SBT itself.

You can bind the `sbt` goal to any phases you like, although only one is recommended for performance. Here we bind to `package`.

Finally, notice that we skip execution of the built-in Maven compile and install plugins, since we're doing everything here with SBT.

## Version Table

| Sbt-Delegate-Maven-Plugin version | SBT version |
| --- | --- |
| 0.1.0 | 1.5.3 |

## License
Apache 2.0
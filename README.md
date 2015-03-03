Gugis-Rx [![Build Status](https://travis-ci.org/lukaszbudnik/gugis-rx.svg?branch=master)](https://travis-ci.org/lukaszbudnik/gugis-rx) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.lukaszbudnik.gugis/gugis-rx/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.lukaszbudnik.gugis/gugis-rx)
==============================

Gugis-Rx is a lightweight and robust framework for creating composite components using Guice.

Gugis-Rx uses Guava and RxJava and works on Java 6+ (Travis runs tests against: openjdk6, openjdk7, oraclejdk7, and oraclejdk8).

The original [Gugis](https://github.com/lukaszbudnik/gugis) uses Java 8 parallel streams and is much faster.
However, as it requires Java 8, it may not be suitable for applications which still run on older Java versions.

# Gugis and Gugis-Rx difference

Gugis-Rx uses RxJava which does not offer parallel `Observable`. Because of this Gugis-Rx does
not offer `Propagation.FASTEST`. The original Gugis unit tests `BasicTest.shouldPropagateToFastest()` and
`ErrorHandlingTest.shouldSelectSlowerBecauseFasterThrowsException()` were commented out.
When/if RxJava will support parallel processing then support for `Propagation.FASTEST` will be also ported to Gugis-Rx.

# Gugis-Rx

Please refer to the original [Gugis](https://github.com/lukaszbudnik/gugis) documentation. With the exception of `Propagation.FASTEST`
both implementations are compatible.

# Examples

See `src/test/java` for lots of unit tests and examples.

# Download

Use the following Maven dependency:

```xml
<dependency>
  <groupId>com.github.lukaszbudnik.gugis</groupId>
  <artifactId>gugis-rx</artifactId>
  <version>{version}</version>
</dependency>
```

or open [search.maven.org](http://search.maven.org/#search|ga|1|com.github.lukaszbudnik.gugis) and copy and paste dependency id for your favourite dependency management tool (Gradle, Buildr, Ivy, sbt, Leiningen, etc).

# Road map

Road map (shared with the original Gugis project) can be viewed on [milestones](https://github.com/lukaszbudnik/gugis/milestones) page.

# License

Copyright 2015 Łukasz Budnik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

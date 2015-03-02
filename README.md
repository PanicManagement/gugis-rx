gugis-rx [![Build Status](https://travis-ci.org/lukaszbudnik/gugis-rx.svg?branch=master)](https://travis-ci.org/lukaszbudnik/gugis-rx) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.lukaszbudnik.gugis/gugis-rx/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.lukaszbudnik.gugis/gugis-rx)
==============================

Guice and RxJava-based lightweight and robust framework for creating composite components.

The original [Gugis](https://github.com/lukaszbudnik/gugis) is written using Java 8 parallel streams and... works on Java 8 only.

Gugis-Rx uses RxJava `Observable` for parallel processing and Guava for fluent iterables. Gugis-Rx runs on Java 6+.

# Gugis-Rx

Please refer to the original [Gugis](https://github.com/lukaszbudnik/gugis) documentation.

# Gugis and Gugis-Rx difference

Original Gugis uses Java 8 parallel streams. Parallel streams turned out to be much faster than RxJava (testing on my MacBook Pro).

The difference can be best illustrated by using `Propagation.FASTEST`. The original Gugis unit test `BasicTest.shouldPropagateToFastest()`
was always passing for the Gugis Java 8 implementation. For Gugis-Rx the same unit test was sometimes failing. To make it
always pass I had to increase difference in sleep methods for `Thread.sleep()` for the sample services.

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

or open [search.maven.org](http://search.maven.org/#search|ga|1|com.github.lukaszbudnik.gugis) and copy and paste dependency id for your favourite dependency management tool (Gradle (gugis uses Gradle), Buildr, Ivy, sbt, Leiningen, etc).

# Road map

Road map can be viewed on [milestones](https://github.com/lukaszbudnik/gugis/milestones) page.

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

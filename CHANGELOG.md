# Change Log: Scalasti, a Scala interface to the StringTemplate library

Version 3.0.0 (**Breaking API Change**)

* Reworked Scalasti API to be immutable. See the [Scalasti home page][]
  for full details.
* Updated ScalaTest dependency to 3.0.1.
* Added _many_ more unit tests.

Version 2.1.4

* Fixed test that broke with upgrade to Grizzled Scala 4.2.0.

Version 2.1.3

* Updated Scala 2.12 cross-compile to 2.12.1.
* Updated Grizzled Scala to 4.2.0.

Version 2.1.2

* Add cross-build for Scala 2.12.0.
* Updated Grizzled Scala dependency to 3.1.0.

Version 2.1.1

* Added unit tests for `Option` and `None`. 
* Converted test suite to use ScalaTest's `FlatSpec`.

Version 2.1.0

* Added support for `Option` values (courtesy of @jalaziz).
* Updated various dependencies.
* Add cross-build for Scala 2.12.0-RC1.
* Removed build dependency on SBT `ls` plugin.
* Added Lightbend Activator, for build simplification.
* Added Travis CI support.

Version 2.0.0

* Reimplemented for StringTemplate v4. **NOTE**: This version is a _complete_
  reimplementation, with a new API that closely mirrors the StringTemplate 4
  API (which is, itself, completely different from StringTemplate 3). See the
  [home page](http://software.clapper.org/scalasti/) for details on the changes.
* Now built for both Scala 2.11 and 2.10.

Version 1.0.0:

* Built for the Scala 2.10.0 series _only_ (2.10.0-RC1, initially). **This
  version, and later versions, are 2.10-only. 2.9.x and earlier will be
  supported via the 0.5.x release branch.** This is due to changes in the
  Scala library between 2.9 and 2.10.
* Updated to SBT 0.12.x.
* Converted to use ScalaTest 2.0, which changes `expect` to `expectResult`.

Version 0.5.8:

* Added Scala 2.9.1-1 to the set of crossbuild versions.
* Minor correction to the handling of multivalue template attributes.

Version 0.5.7:

* The `StringTemplate` class now has a constructor that accepts a `Seq[Char]`,
  which supports a `Stream[Char]`. Addresses [Issue #4][].

[Issue #4]: https://github.com/bmc/scalasti/issues/4

Version 0.5.6:

* Internal change to `StringTemplate.setAttribute()` method.
* Now compiles for Scala 2.8.2, in addition to 2.8.0, 2.8.1, 2.9.0, 2.9.0-1 and
  2.9.1.
* Converted to build with SBT 0.11.2.
* Added support for `ls.implicit.ly` metadata.
* Now publishes to `oss.sonatype.org` (and, thence, to the Maven central repo).
* Bumped version deps for [Grizzed Scala][] and [ClassUtil][].

[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[ClassUtil]: http://software.clapper.org/classutil/

Version 0.5.5:

* Now builds for [Scala][] 2.9.1, as well as 2.9.0-1, 2.9.0, 2.8.1, and 2.8.0.
* Converted code to conform with standard Scala coding style.
* Fixed some minor Scaladoc issues.

[Scala]: http://www.scala-lang.org/

Version 0.5.4:

* Now builds against Scala 2.9.0.1, as well as Scala 2.9.0, 2.8.1 and 2.8.0.
* Converted to build with [SBT][] 0.10.1

Version 0.5.3:

* Now builds against Scala 2.9.0, as well as Scala 2.8.0 and 2.8.1.
* Internal clean-up of reified type handling, used by Scalasti when setting
  converting Scala values to Java counterparts.
* Updated to version 1.4.1 of [ScalaTest][] for Scala 2.9.0. (Still uses
  ScalaTest 1.3, for Scala 2.8).
* Updated to use [SBT][] 0.7.7.
* Updated to version 1.0.6 of the [Grizzled Scala][] library.
* Updated to version 0.3.6 of the [ClassUtil][] library.

[ScalaTest]: http://www.scalatest.org/
[SBT]: http://code.google.com/p/simple-build-tool/
[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[ClassUtil]: http://software.clapper.org/classutil/

Version 0.5.2:

* Updated to latest release of [ClassUtil][] library.
* Updated to latest release of [sbt-plugins][] library.

[ClassUtil]: http://software.clapper.org/classutil/
[sbt-plugins]: http://software.clapper.org/sbt-plugins/

Version 0.5.1:

* Now compiles against [Scala][] 2.8.1, as well as 2.8.0
* Updated to version 0.3.2 of the [ClassUtil][] library, to pick up a fix
  to [ClassUtil Issue #1][] (The ClassUtil `MapToBean` method can generate
  class names that conflict with previously generated class names.)

[Scala]: http://www.scala-lang.org/
[ClassUtil]: http://software.clapper.org/classutil/
[ClassUtil Issue #1]: http://github.com/bmc/classutil/issues#issue/1

Version 0.5:

* Added `makeBeanAttribute()` methods, which automatically map Scala objects
  to Java Beans for use with StringTemplate. Addresses Scalasti [Issue #1][].
* Moved `isOfType` method to the [ClassUtil][] library.
* Updated to released 1.2 version of [ScalaTest][] and updated the dependency
  to be a test, rather than runtime, one.
* Moved unit testers into package-specific subdirectories.
* Now published to the [Scala Tools Maven repository][], which [SBT][]
  includes by default. Thus, if you're using SBT, it's longer necessary to
  specify a custom repository to find this artifact.

[Issue #1]: http://github.com/bmc/scalasti/issues#issue/1
[ScalaTest]: http://scalatest.org/
[ClassUtil]: http://software.clapper.org/classutil/
[Scala Tools Maven repository]: http://www.scala-tools.org/repo-releases/
[SBT]: http://code.google.com/p/simple-build-tool/

Version 0.4.1:

* Updated to build with Scala 2.8.0.final *only*. Dropped support for
  Scala 2.8.0 release candidates (RCs).


Version 0.4:

* Added support for [mapped aggregates][]. The
  `StringTemplate.setAggregate(name, map)` method now converts the map to a
  Java Bean and support nested maps. Conversion is done via the
  [ClassUtil][] library's [MapToBean][] capability. This change introduces
  a dependency on [ClassUtil][].

[ClassUtil]: http://software.clapper.org/classutil/
[MapToBean]: http://software.clapper.org/classutil/#generating_java_beans_from_scala_maps
[mapped aggregates]: http://darkroom/~bmc/mystuff/scala/scalasti/gh-pages/_site/#mapped_aggregates

Version 0.3.1:

* Updated to version 0.7.2 of [Grizzled Scala][].
* Now compiles with Scala 2.8.0.RC5, as well as RC3. Dropped support for RC2.

[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[automatic aggregate creation]: http://www.antlr.org/wiki/display/ST/Expressions#Expressions-Automaticaggregatecreation

Version 0.3:

* Added support for [automatic aggregate creation][], via the new
  `StringTemplate.setAggregate()` methods.
* Changed `StringTemplate` interface to support `Any`, not just `AnyRef`
  attribute values.
* Various `set` methods in `StringTemplate` now return the template.
* Updated to version 0.7 of [Grizzled Scala][].
* Now compiles with Scala 2.8.0.RC3, as well as RC2. Dropped support for RC1.

[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[automatic aggregate creation]: http://www.antlr.org/wiki/display/ST/Expressions#Expressions-Automaticaggregatecreation

Version 0.2.1:

* Updated to build with Scala 2.8.0.RC2, as well as Scala 2.8.0.RC1.
* Maven artifact now includes Scala version (e.g., `scalasti_2.8.0.RC2`,
  instead of `scalasti`).
* Enhanced Scaladocs a bit.

[scalaj-collection]: http://github.com/scalaj/scalaj-collection

Version 0.2:

* Updated to build with Scala 2.8.0.RC1
* Updated to `posterous-sbt` plugin, version 0.1.5.
* Removed dependency on [scalaj-collection][].
* Corrected SBT build's retrieval of 2.7.7 (old) Grizzled-Scala, needed for
  SBT build file to compile.

[scalaj-collection]: http://github.com/scalaj/scalaj-collection

Version 0.1:

* Initial release

[Scalasti home page]: http://software.clapper.org/scalasti/

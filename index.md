---
title: Scalasti, a Scala interface to the Java StringTemplate library
layout: withTOC
---

# Introduction

Scalasti is a [Scala][] interface to the [StringTemplate][] Java template
library. It provides a subset of the features of [StringTemplate][], using
a more Scala-friendly syntax.

# Rationale

[StringTemplate][] is a Java-based template engines, comparable in
functionality to APIs like [Google's Closure Templates][], [FreeMarker][]
and [Velocity][]. There are also Scala-based template engines, such as
[Scalate][] (a powerful template language that bills itself as being "like
JSP without the crap, but with added Scala coolness.")

The problem with most template languages, though, is that they're a little
(or a lot) too powerful. As Terence Parr, creator of StringTemplate, wrote,
in his paper
[*Enforcing Strict Model-View Separation in Template Engines*][]:

> The mantra of every experienced web application developer is the same:
> thou shalt separate business logic from display. Ironically, almost all
> template engines allow violation of this separation principle, which is
> the very impetus for HTML template engine development. This situation is
> due mostly to a lack of formal definition of separation and fear that
> enforcing separation emasculates a template’s power.
>
> ...
>
> Without exception, programmers espouse separation of logic and display as
> an ideal principle. In practice, however, programmers and engine
> producers are loath to enforce separation, fearing loss of power
> resulting in a crucial page that they cannot generate while satisfying
> the principle. Instead, they encour- age rather than enforce the
> principle, leaving themselves a gaping “backdoor” to avoid insufficient
> page generation power. Unfortunately, under deadline pressure,
> programmers will use this backdoor routinely as an expedient if it is
> available to them, thus, entangling logic and display.
>
> ...
>
> The opposite situation is more common where programmers em- bed business
> logic in their templates as an expedient to avoid having to update their
> data model. Given a Turing-complete template programming language,
> programmers are tempted to add logic directly where they need it in the
> template instead of having the data model do the logic and passing in the
> boolean result, thereby, decoupling the view from the model.

Allowing arbitrary and powerful code in a template just invites disaster. I
want a solid template language that is:

* free of side-effects, and
* reduces or eliminates the temptation to put business logic in the
  template.

StringTemplate fits the bill nicely.

I want to use StringTemplate from Scala, which is eminently feasible, since
there's a Java version of StringTemplate. However, StringTemplate relies on
`java.util.Collection` classes, such as `java.util.Map` and
`java.util.List`; these classes are clumsy to use in Scala, compared to
their Scala counterparts. I created the Scalasti wrapper interface to
expose StringTemplate capabilities in a more Scala-friendly way.

# Installation

Scalasti is published to the `oss.sonatype.org` repository and automatically
sync'd with the [Maven Central Repository][].

* Version 1.0.0 supports Scala 2.10.0-RC1
* Version 0.5.8 supports Scala 2.9.1-1, 2.9.1, 2.9.0-1, 2.9.0, 2.8.2, 2.8.1 and
  2.8.0.

## Installing for Maven

If you're using [Maven][], just specify the artifact, and Maven will do the
rest for you:

* Group ID: `org.clapper`
* Artifact ID: `scalasti_2.9.1` or `scalasti_2.10`
* Version: `0.5.8` or `1.0.0`
* Type: `jar`

Here's a sample Maven POM "dependency" snippet:

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>scalasti_2.10</artifactId>
      <version>1.0.0</version>
    </dependency>

For more information on using Maven and Scala, see Josh Suereth's
[Scala Maven Guide][].

## Using with SBT

#### 0.7.x

If you're using [SBT][] 0.7.x to compile your code, you can place the
following line in your project file (i.e., the Scala file in your
`project/build/` directory):

    val scalasti = "org.clapper" %% "scalasti" % "0.5.8"

#### 0.11.x

If you're using [SBT][] 0.11.x or 0.12.x to compile your code, you can use the
following line in your `build.sbt` file (for Quick Configuration). If
you're using an SBT 0.11.x Full Configuration, you're obviously smart
enough to figure out what to do, on your own.

    libraryDependencies += "org.clapper" %% "scalasti" % "0.5.8"

NOTE: For release candidates of Scala 2.10, you'll have to specify the version
explicitly:

    libraryDependencies += "org.clapper" % "scalasti_2.10" % "1.0.0"

Scalasti is also registered with [Doug Tangren][]'s excellent
[ls.implicit.ly][] catalog. If you use the `ls` SBT plugin, you can install
Scalasti with

    sbt> ls-install scalasti

# Building from Source

## Source Code Repository

The source code for the Scalasti library is maintained on [GitHub][]. To
clone the repository, run this command:

    git clone git://github.com/bmc/scalasti.git

## Build Requirements

Building the Scalasti library requires [SBT][] 0.10.1 or better. Install
SBT, as described at the SBT web site.

## Building Scalasti

Assuming you have an `sbt` shell script (or .BAT file, for *\[shudder\]*
Windows), first run:

    sbt compile test package

The resulting jar file will be in the top-level `target` directory.

# Runtime Requirements

Scalasti requires the following libraries to be available at runtime, for
 some, or all, of its methods.
 
* The main [ASM][] library (version 3), e.g., `asm-3.2.jar`
* The [ASM][] commons library (version 3), e.g., `asm-commons-3.2.jar`
* The [ClassUtil][] library
* The [Grizzled Scala][] library
* The [Grizzled SLF4J][] library, for logging
* The [SLF4J][] API library, for logging (e.g., `slf4j-api-1.5.11.jar`)
* An SLF4J implementation, such as [Logback][] or [AVSL][], if you want
  logging.

Maven and [SBT][] should automatically download these libraries for you.

# Using Scalasti

The Scalasti API provides simple wrappers around the most common classes
in the [StringTemplate][] API. For various reasons, subclassing the
StringTemplate classes is non-trivial, so Scalasti's classes are wrappers
that delegate their operations to the wrapped StringTemplate object. Since
Scalasti does not provide the full suite of capabilities available in the
actual StringTemplate classes, you can, at any point, retrieve a copy of
the actual underlying StringTemplate API object, so you can interact
directly with it.

## Simple Examples

Create a template group that will read templates from a directory:

    val group = new StringTemplateGroup("mygroup", new File("/tmp"))
    val template = group.template("mytemplate")
    
Create a template group that will load templates from the CLASSPATH:

    val grp = new StringTemplateGroup("mygroup")
    val template = group.template("org/clapper/templates/mytemplate")

Set attribute values one by one:

    // A single-valued attribute:
    template.setAttribute("title", "Employees")
    
    // A multi-valued attribute:
    template.setAttribute("employees", "Moe", "Larry", "Curley")

Set attribute values all at once:

    template.setAttributes(Map("title" -> "Employees",
                               "employees" -> List("Moe", "Larry", "Curley")))

Change how an attribute is rendered:

    class HexValue(l: long)
    class HexValueRenderer extends AttributeRenderer[HexValue] {
      def toString(v: HexValue) = "0x" + v.toHexString
    }

    val memoryLocation: Long = ...
    template.setAttribute("hexidecimal address", new HexValue(memoryLocation))
    template.setAttribute("decimal address", memoryLocation)
    template.registerRenderer(new HexValueRenderer)

Render a template with its current set of attributes:

    println(template.toString)

## Support for Aggregates

The underlying [StringTemplate][] API supports the notion of
aggregates--attributes that, themselves, have attributes.
[StringTemplate][] supports aggregates via Java Bean objects and
[automatic aggregates][]. Scalasti also supports Java Bean objects and
automatic aggregates, but it adds support for two other kinds of
aggregates:

* mapped aggregates: aggregates created, on the fly, from maps that are
  recursively wrapped in Java Beans
* bean attributes: Scala objects that are recursively wrapped, on the fly,
  in Java Beans
  
You'll find more information on these two enhanced forms of aggregates
further down in this document.

There are two forms of the `setAggregate()` method, as described below.

### Automatic aggregates

As the StringTemplate documentation puts it:

> Creating one-off data aggregates is a pain, you have to define a new
> class just to associate two pieces of data. `StringTemplate makes` it
> easy to group data during `setAttribute()` calls. You may pass in an
> aggregrate attribute name to `setAttribute()` with the data to aggregate
> \[as in this Java code fragment\]:
>
>        StringTemplate st = new StringTemplate("$items:{$it.(\"last\")$, $it.(\"first\")$\n}$");
>        st.setAttribute("items.{first,last}", "John", "Smith");
>        st.setAttribute("items.{first,last}", "Baron", "Von Munchhausen");
>        String expecting = "Smith, John\nVon Munchhausen, Baron\n";


Scalasti provides support for these automatic aggregates, though, for
clarity, Scalasti names the methods `setAggregate()`, instead of
overloading `setAttribute()` for aggregates.

The first form of `StringTemplate.setAggregate()` handles automatic
aggregates. The automatic aggregates mirrors, almost exactly, what the
underlying StringTemplate library does:

    def setAggregate(aggrSpec: String, values: Any*): StringTemplate

It sets an automatic aggregate from the specified arguments, returning the
template, for convenience. An automatic aggregate looks like an object from
within a template, but it isn't backed by a bean. Instead, you specify the
aggregate with a special syntax. For example, the following code defines
an aggregate attribute called `name`, with two fields, `first` and `last`.
Those fields can be interpolated within a template via `$item.first$` and
`$item.last$`.

    val st = new StringTemplate( ... )
    st.setAggregate("name.{first,last}", "Moe", "Howard")

That aggregate permits the following template references:

    $name.first$
    $name.last$

Setting the same aggregate multiple times results in a list of aggregates:

    val st = new StringTemplate( ... )
    st.setAggregate("name.{first,last}", "Moe", "Howard")
    st.setAggregate("name.{first,last}", "Larry", "Fine")
    st.setAggregate("name.{first,last}", "Curley", "Howard")

Note, however, that this syntax does not support nested aggregates. That is,
there is no way, using automatic aggregates, to produce an attribute that
can be referenced like this:

    $foo.outer.inner$

For that capability, you need mapped aggregates. (See next section.)

### Mapped Aggregates

Scalasti adds another form of aggregate attribute called a "mapped
aggregate". Mapped aggregates are simply aggregate attributes created from
Scala maps. The supplied map's keys are used as the fields of the
aggregate. The mapped aggregates feature allows you to create a map, like
this:

    st.setAggregate("myfield", Map("foo" -> List(1, 2), "bar" -> "barski"))

and then access it in the template like this:

    $myfield.bar$
    <ul>
      $myfield.foo:{ item | <li>$listitem(item)$</li>$\n$}$
    </ul>

The second form of the `StringTemplate.setAggregate()` method handles
mapped aggregates:

    def setAggregate(attrName: String, valueMap: Map[String, Any]): StringTemplate

With this version of `setAggregate()`, the supplied map's keys are used as
the fields of the aggregate. With a mapped aggregate, Scalasti actually
translates the map into a Java Bean, which it then uses to set the
attribute. Because Scalasti recursively converts all maps it finds (as
long as they are of type `Map[String, Any]`), a mapped attribute can handle
nested attribute references.

**NOTE**: The underlying [StringTemplate][] library does *not* support the
notion of a mapped aggregate; mapped aggregates are a Scalasti add-on.

For example, given this map:

    Map("foo" -> List(1, 2), "bar" -> "barski")

and the name "mystuff", this method will produce the equivalent of the
following call:

    template.setAggregate("mystuff.{foo, bar}", List(1, 2), "barski")

However, it does so by creating a Java Bean, not by using the underlying
StringTemplate library's automatic aggregate feature.

In addition, mapped aggregates support nested maps. For instance, this code
fragment:

    val attrMap = Map("foo"   -> "FOO",
                      "alien" -> Map("firstName" -> "John",
                                     "lastName"  -> "Smallberries"))
    template.setAggregate("thing", attrMap)

will make the following values available in a template:

    $thing.foo$                  # expands to "FOO"
    $things.alien.firstName$     # expands to "John"
    $things.alien.lastName$      # expands to "Smallberries"

To convert the map to a Java Bean, Scalasti uses the [ClassUtil][]
library's [`MapToBean`][MapToBean] capability.

### Scala Bean Aggregates

Normally, StringTemplate expects non-primitive attributes to be either
collections or Java Beans. In Scala, you can force a class to have Java Bean
getters and setters by marking fields with the `@BeanProperty` annotation.
However, sometimes that's annoying or even impossible. For example, if you
have an instance of a final Scala class from some third party API, you
can't necessarily change that class to add `@BeanProperty` to the fields
you want StringTemplate to see; writing a wrapper class is usually your
only option. Similarly, if you have a Scala case class, often expressed in
a single line of code, extending it to multiple lines of code, just to add
`@BeanProperty`, is annoying.

To solve this problem, Scalasti provides a couple variants of a
`makeBeanAttribute()` method, which takes a Scala object and recursively
wraps it in a Java Bean, before passing it to StringTemplate. Here are
the declarations for `makeBeanAttribute()`:

    def makeBeanAttribute[T](attrName: String, values: T*): StringTemplate
    def makeBeanAttribute[T](attrName: String, values: Iterator[T]): StringTemplate

Unlike the `setAttribute()` methods, the `makeBeanAttribute()` methods
automatically convert the Scala object values to Java Beans, using the
[ClassUtil][] library's `ScalaObjectToBean` capability. Thus, using
`makeBeanAttribute()` allows you to pass Scala objects to StringTemplate,
without using the `@BeanProperty` annotation to generate Java Bean getters
for StringTemplate to use.

**NOTE**: This capability requires the presence of the ASM byte code
generation library at runtime.

Here's an example, adapted from the Scalasti unit tests.

    case class Outer(inner: String, x: Int)
    case class Thing(outer: Outer, okay: String)
    case class Foo(bar: String, baz: Int)

    val template = "$thing.outer.inner$ $foo.bar$ $foo.baz$ " +
                   "$thing.outer.x$ $thing.okay$"

    val thing = Thing(Outer("some string thing", 10), "OKAY")
    val foo = Foo("BARSKI", 42)

    val st = new StringTemplate(template).makeBeanAttribute("thing", thing).
                                          makeBeanAttribute("foo", foo)
    println(st.toString)

    // Prints: some string thing BARSKI 42 10 OKAY

## Access to Underlying `StringTemplate`

At any point, you can retrieve the underlying [StringTemplate][] API's
`StringTemplate` object, via a call to the `nativeTemplate` method:

    val st = new StringTemplate(template).makeBeanAttribute("thing", thing).
                                          makeBeanAttribute("foo", foo)
    ...
    val nativeTemplate = st.nativeTemplate

Once you have the native template, you can interact with it using the
methods it exposes (i.e., the [StringTemplate][] Java API methods).

For various internal reasons, the returned native template is a _copy_ of the
underlying `StringTemplate` object. Thus, if you plan to retrieve the native
template, do your Scala work first:

* Instantiate a Scalasti `StringTemplate` object.
* Add values to the template via the Scalasti `StringTemplate` object.
* Call `nativeTemplate`, to get copy of the underlying (real) template.
* Add to the underlying template, using Java semantics.
* Render the template with native template copy.

## API Documentation

The Scaladoc-generated the [API documentation][] is available locally.
In addition, you can generate your own version with:

    sbt doc

## Additional Documentation

Consult the [StringTemplate documentation][] for complete details on creating,
deploying, and using StringTemplate templates.

# Author

[Brian M. Clapper][]

# Contributing to Scalasti

Scalasti is still under development. If you have suggestions or
contributions, feel free to fork the [Scalasti repository][repo], make your
changes, and send me a pull request.

# Copyright and License

Scalasti is copyright &copy; 2010-2011 Brian M. Clapper and is released
under a [BSD License][].

# Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [Scalasti repository][repo] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the Scalasti project
  under a [BSD License][].

[API documentation]: api/
[BSD License]: license.html
[Scala]: http://www.scala-lang.org/
[StringTemplate]: http://www.stringtemplate.org/
[StringTemplate documentation]: http://www.antlr.org/wiki/display/ST/StringTemplate+Documentation
[Scalasti]: http://software.clapper.org/scalasti
[Scalasti home page]: http://software.clapper.org/scalasti
[repo]: http://github.com/bmc/scalasti
[Brian M. Clapper]: mailto:bmc@clapper.org
[Google's Closure Templates]: http://code.google.com/closure/templates/index.html
[Velocity]: http://velocity.apache.org/
[FreeMarker]: http://www.freemarker.org/
[Scalate]: http://scalate.fusesource.org/
[*Enforcing Strict Model-View Separation in Template Engines*]: http://www.cs.usfca.edu/~parrt/papers/mvc.templates.pdf
[SBT]: http://code.google.com/p/simple-build-tool
[Maven]: http://maven.apache.org/
[GitHub repository]: http://github.com/bmc/scalasti
[GitHub]: http://github.com/bmc/
[SBT cross-building]: http://code.google.com/p/simple-build-tool/wiki/CrossBuild
[automatic aggregates]: http://www.antlr.org/wiki/display/ST/Expressions#Expressions-Automaticaggregatecreation
[ClassUtil]: http://software.clapper.org/classutil/
[Grizzled Scala]: http://software.clapper.org/grizzled-scala/
[Grizzled SLF4J]: http://software.clapper.org/grizzled-slf4j/
[MapToBean]: http://software.clapper.org/classutil/#generating_java_beans_from_scala_maps
[AVSL]: http://software.clapper.org/avsl/
[ASM]: http://asm.ow2.org/
[SLF4J]: http://slf4j.org/
[Scala Maven Guide]: http://www.scala-lang.org/node/345
[changelog]: https://github.com/bmc/scalasti/blob/master/CHANGELOG.md
[logback]: http://logback.qos.ch/
[Maven central repository]: http://search.maven.org/
[ls.implicit.ly]: http://ls.implicit.ly

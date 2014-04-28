---
title: Scalasti, a Scala interface to the Java StringTemplate library
layout: withTOC
---

# Introduction

Scalasti is a [Scala][] interface to the [StringTemplate][] Java template
library. It provides a subset of the features of [StringTemplate][], using
a more Scala-friendly syntax.

**If you're upgrading from Scalasti 1.0.0 and StringTemplate 3, see the
[Upgrading](#upgrading-from-scalasti-version-1-and-stringtemplate-3)
section, below.**

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

StringTemplate provides a solid template language that is:

* free of side-effects, and
* reduces or eliminates the temptation to put business logic in the
  template.

However, StringTemplate's API is Java-centric. It relies on
`java.util.Collection` classes, such as `java.util.Map` and `java.util.List`;
these classes are clumsy to use in Scala, compared to their Scala counterparts.
Worse, StringTemplate uses Java Bean semantics to access object field values.
While Java Bean semantics are great for Java objects, they're not so good for
Scala objects. With an API that only supports Java Bean semantics, you can only
use Scala objects you can annotate with the Scala `@BeanProperty` annotation.
All other objects (e.g., those from APIs you do not control) must be manually
wrapped in Java Beans.

The Scalasti wrapper library alleviates those pain points. It automatically maps
Scala collections to their underlying Java counterparts, and it automatically
wraps Scala objects in dynamically generated Java Beans. If you use Scalasti,
you can use StringTemplate with Scala objects and collections, without wrapping
them yourself.

# Installation

Scalasti is published to the
[Bintray Maven repository](https://bintray.com/bmc/maven), which is
automatically linked to Bintray's [JCenter](https://bintray.com/bintray/jcenter)
repository. (From JCenter, it's eventually pushed to the
automatically sync'd with the [Maven Central Repository][].

* Version 2.0.0 (for StringTemplate version 4) supports Scala 2.11 and 2.10.
* Version 1.0.0 (for StringTemplate version 3) supports Scala 2.10.
* Version 0.5.8 (for StringTemplate version 3) supports Scala 2.9.1-1, 2.9.1,
  2.9.0-1, 2.9.0, 2.8.2, 2.8.1 and 2.8.0.

## Installing for Maven

If you're using [Maven][], just specify the artifact, and Maven will do the
rest for you:

* Group ID: `org.clapper`
* Artifact ID: `scalasti_2.10` or `scalasti_2.11`
* Version: `1.0.0` or `2.0.0`
* Type: `jar`

Here's a sample Maven POM "dependency" snippet:

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>scalasti_2.10</artifactId>
      <version>2.0.0</version>
    </dependency>

For more information on using Maven and Scala, see Josh Suereth's
[Scala Maven Guide][].

## Using with SBT

#### 0.11.x/0.12.x

If you're using [SBT][] 0.11.x or 0.12.x to compile your code, you can use the
following line in your build.sbt file (for Quick Configuration).

    repositories += "JCenter" at "http://jcenter.bintray.com/"

    libraryDependencies += "org.clapper" %% "scalasti" % "2.0.0"

You only need the `repositories` line if the artifact cannot be resolved (e.g.,
has not, for some reason, been pushed to Maven Central yet).

#### 0.13.x

With SBT 0.13.x, you can just use [Doug Tangren's](https://github.com/softprops/)
`bintray-sbt` plugin. In your `project/plugins.sbt` file, add:

    resolvers += Resolver.url(
      "bintray-sbt-plugin-releases",
      url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

    addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

Then, in your `build.sbt` file, add:

    bintrayResolverSettings

That automatically adds the appropriate Bintray repositories. Finally, add:

    libraryDependencies += "org.clapper" %% "scalasti" % "2.0.0"

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

Scalasti 2.0.0 requires the following libraries to be available at runtime, for
some, or all, of its methods.

* The main [ASM][] library (version 4), e.g., `asm-4.2.jar`
* The [ASM][] commons library (version 4), e.g., `asm-commons-4.2.jar`
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

Create a template group that will read templates from a directory, and load
a template from the group:

    val group = STGroupDir("/home/bmc/templates")
    val templateTry = group.instanceOf("mytemplate")

    // instanceOf returns a scala.util.Try.
    if (t.isSuccess) {
      ...
    }

Create a template group whose contents are in a string, and retrieve a template:

    val templateString =
      """
      |foo(firstName, lastName) ::= <<
      |This is a test template. It spans multiple lines, and it interpolates
      |a first name (<firstName>) and a last name (<lastName>).
      """.stripMargin

    val group = STGroupString(templateString)
    val template = group.instanceOf("foo")

    // instanceOf returns a scala.util.Try.
    if (t.isSuccess) {
      ...
    }

Read a template group from a single file, and retrieve a template:

    val group = STGroupFile("/home/bmc/templates/foo.stg")
    val template = group.instanceOf("foo")

    // instanceOf returns a scala.util.Try.
    if (t.isSuccess) {
      ...
    }

Read a template group from a URL, and retrieve a template:

    import java.net.URL

    val group = STGroupFile(new URL("http://localhost/~bmc/templates/foo.stg")
    val template = group.instanceOf("foo")

    // instanceOf returns a scala.util.Try.
    if (t.isSuccess) {
      ...
    }

Create a template on the fly:

    val template = ST("Test template. <firstName> <lastName>")

Set attribute values one by one:

    // A single-valued attribute:
    template.add("firstName", "Moe")
    template.add("lastName", "Howard")
    
    // A multi-valued attribute:
    template.setAttribute("employees", "Moe", "Larry", "Curley")

Set attribute values all at once:

    template.addAttributes(Map("firstName" -> "Moe", "lastName" -> "Howard"))

Change how an attribute is rendered:

    class HexValue(l: long)
    class HexValueRenderer extends AttributeRenderer[HexValue] {
      def toString(v: HexValue, format: String, locale: Locale) = {
        "0x" + v.toHexString
      }
    }

    val memoryLocation: Long = ...
    template.add("hex", new HexValue(memoryLocation))
    template.add("decimal", memoryLocation)
    template.registerRenderer(new HexValueRenderer)

Render a template with its current set of attributes:

    println(template.render())

## Support for Aggregates

The underlying [StringTemplate][] API supports the notion of
aggregates—attributes that, themselves, have attributes.
[StringTemplate][] supports aggregates via Java Bean objects and
[automatic aggregates][]. Scalasti also supports Java Bean objects and
automatic aggregates, but it adds support for two other kinds of
aggregates:

* _mapped aggregates_: aggregates created, on the fly, from maps that are
  recursively wrapped in Java Beans
* _bean attributes_: Scala objects that are recursively wrapped, on the fly,
  in Java Beans
  
You'll find more information on these two enhanced forms of aggregates
further down in this document.

There are two forms of the `setAggregate()` method, as described below.

### Automatic aggregates

To paraphrase the older StringTemplate documentation,
creating one-off data aggregates is a pain; you have to define a new
class just to associate two pieces of data. `StringTemplate` makes it
easy to group data via a concept called _aggregates_. Using the
`ST.addAggregate()` method, you can associate a group of values with
fields in one shot:

    val st = ST("<page.title>\n\n<page.body>\n")
    st.addAggregate("page.{title,body}", title, body)

Scalasti provides support for these automatic aggregates, though, for clarity,
Scalasti names the method `addAggregate()`, instead of StringTemplate's
`addAggr()`.

`addAggregate()` handles automatic aggregates. The automatic aggregates mirrors,
almost exactly, what the underlying StringTemplate library does:

    def addAggregate(aggrSpec: String, values: Any*): ST

It sets an automatic aggregate from the specified arguments, returning the
template, for convenience. An automatic aggregate looks like an object from
within a template, but it isn't backed by a bean. Instead, you specify the
aggregate with a special syntax. For example, the following code defines
an aggregate attribute called `name`, with two fields, `first` and `last`.
Those fields can be interpolated within a template via `<name.first>` and
`<name.last>`.

    val st = new StringTemplate( ... )
    st.setAggregate("name.{first,last}", "Moe", "Howard")

That aggregate permits the following template references:

    <name.first>
    <name.last>

Note, however, that this syntax does not support nested aggregates. That is,
there is no way, using automatic aggregates, to produce an attribute that
can be referenced like this:

    <foo.outer.inner>

For that capability, you need mapped aggregates. (See next section.)

### Mapped Aggregates

Scalasti adds another form of aggregate attribute called a "mapped
aggregate". Mapped aggregates are simply aggregate attributes created from
Scala maps. The supplied map's keys are used as the fields of the
aggregate. The mapped aggregates feature allows you to create a map, like
this:

    st.addMappedAggregate("myfield", Map("foo" -> List(1, 2), "bar" -> "barski"))

and then access it in the template like this:

    $myfield.bar$
    <ul>
      $myfield.foo:{ item | <li>$item$</li>$\n$}$
    </ul>

This example assumes that the `ST` object was created with a start and stop
delimiter of '$', instead of the default '<' and '>'.

The supplied map must use string keys; the values are mapped to Java objects
in a similar way as `add()` maps values. (See below.)

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

To solve this problem, Scalasti automatically converts attributes to Java
Beans, using the [ClassUtil][] library.

Sometimes, however, you don't want that automatic wrapping. If you're adding
an object that already supports Java Bean semantics, that extra wrapping is
pointless. Also, if you're using an `AttributeRenderer` to control how values
of a particular type are converted to string, this automatic wrapping will
get in the way. With an `AttributeRenderer`, StringTemplate matches the values
to the attributes by matching classes; the automatic Bean-wrapping breaks that
matching.

To get around that problem, you can add _raw_ attributes to a template, by
specifying a special third `raw` parameter to the `add()` method. The parameter
defaults to `false`.

    st.add("foo", foo)           // foo gets wrapped in a Java Bean
    st.add("bar", bar, raw=true) // bar gets added without wrapping

**NOTE**: The wrapping capability requires the presence of the ASM byte code
generation library at runtime.

## Access to Underlying `StringTemplate`

At any point, you can retrieve the underlying [StringTemplate][] API's
`StringTemplate` object, via a call to the `nativeTemplate` method:

    val st = ST(template).add("thing", thing).add("foo", foo)
    val nativeTemplate = st.nativeTemplate

Once you have the native template, you can interact with it using the
methods it exposes (i.e., the [StringTemplate][] Java API methods).

## Upgrading from Scalasti version 1 (and StringTemplate 3)

As of version 2.0.0, Scalasti is based on [StringTemplate][] 4. Scalasti has
been entirely rewritten for StringTemplate 4. While it retains the same basic
functionality as Scalasti 1.0.0, the Scalasti 2.0.0 mimics the StringTemplate 4
API, which is significantly different from StringTemplate 3.

Here are some of the specific differences:

### Class Names

Scalasti 1.0.0:

    StringTemplate      // a string template
    StringTemplateGroup // a string template group

Scalasti 2.0.0

    ST                  // a string template
    STGroup             // base string template group class
    STGroupString       // template group created from a string
    STGroupDir          // template group(s) created from a directory tree
    STGroupFile         // template group created from a file

### Creating template groups

Scalasti 1 provided a `StringTemplateGroup` class, which could be directly
instantiated. Scalasti 2 provides several template group classes, which are
instantiated via companion object `apply()` methods.

#### Load a template from a director or directory tree

Instead of:

    // Load a template group hierarchy
    val grp = new StringTemplateGroup("name", new File("/home/bmc/templates"))

use:
    // Load a template group hierarchy
    val group = STGroupDir("/home/bmc/templates")

#### Load a template from a file

Instead of:

    val grp = new StringTemplateGroup("name", new File("/home/bmc/templates/test.stg"))

use:

    val grp = STGroupFile("/home/bmc/templates/test.stg")

#### Load a template from the CLASSPATH

Instead of:

    val grp = new StringTemplateGroup("name")
    val template = grp.template("org/clapper/templates/mytemplate")

use:

    val grp = STGroupFile("org/clapper/templates/mytemplate.stg")

Note that, in this case, `STGroupFile` will attempt to find (relative) file
"org/clapper/templates/mytemplate.stg"; if it cannot find the file, it will
search the CLASSPATH for a corresponding resource.

### Instantiating a template

#### Creating a template from a string

Instead of:

    val st = new StringTemplate("...")

use:

    val st = ST("...")

#### Instantiating a named template from a group

Instead of:

    val st = group.template("org/clapper/templates/foo")

use:

     val st = group.instanceOf("org/clapper/templates/foo")

### Setting attributes on a template

Scalasti 1.0.0 (and StringTemplate 3) overloaded the `setAttribute()` method
to set various kinds of attributes. Scalasti 2.0.0 and StringTemplate 4
use different methods. Among other things, the `makeBeanAttribute()` methods
are no longer available; the capability has been rolled directly into the
`add()` method.

Summary of changes:

* Scalasti 2's `setAttribute()` method has been replaced by `add()` and
  `set()`.
* `makeBeanAttribute()` is now built into the `add()` method, and the behavior
  can be disabled by specifying `raw=true`.
* `registerRenderer()` now takes the type that the renderer is to render.
* The group `template()` method, to retrieve a template from a group, has
  been replaced by `instanceOf()`.
* `toString()` on a template no longer renders the template. Use the new
  `render()` method, instead.
* `setAggregate()` has been replaced by `addAggregate()`.

Some specific examples:

     val st = group.instanceOf("...")

    // Add an attribute, wrapping it in a Java Bean if necessary
    st.add("foo", new MyValue(...))
    st.add("num", 10)
    st.add("name", "Brian")

    // Add an attribute, forcing Scalasti not to wrap it in a Java Bean.
    // Necessary for use with AttributeRenderers
    st.add("foo", new MyValue(...), raw=true)

    // Display the rendered template.
    println(st.render())

## API Documentation

For full details, see the [API Documentation][].

In addition, you can generate your own version with:

    sbt doc

## Additional Documentation

Consult the [StringTemplate documentation][] for complete details on creating,
deploying, and using StringTemplate templates.

# Author

[Brian M. Clapper][]

# Contributing to Scalasti

If you have suggestions or contributions, feel free to fork the
[Scalasti repository][repo], make your changes, and send me a pull request.

# Copyright and License

Scalasti is copyright &copy; 2010-2014 Brian M. Clapper and is released
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

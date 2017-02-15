---
title: Scalasti, a Scala interface to the Java StringTemplate library
layout: withTOC
---

# Introduction

Scalasti is a [Scala][] interface to the [StringTemplate][] Java template
library. It provides a subset of the features of [StringTemplate][], using
a more Scala-friendly syntax.

Scalasti's additional features include:

- **Immutability**. As of version 3.0.0, Scalasti objects are immutable, 
  unlike the StringTemplate objects. Modifier methods always create new 
  objects; they never modify objects in place. Immutability is more functional
  and more idiomatic to Scala, and an immutable Scalasti API was long overdue.

- **Error-handling**. Where possible, Scalasti propagates errors
  via `scala.util.Try` objects, instead of via a StringTemplate
  listener. This approach is also more idiomatic to Scala.

- **Scala object support**. Scalasti supports Scala objects, meaning
  you don't have to use `@BeanProperty` on your Scala classes before you
  can pass them into a template. This feature also allows you to use
  instances of third-party Scala classes directly with Scalasti.

- **Stronger type safety**. You should _never_ need to cast objects
  you receive from the Scalast API.

- **Mapped aggregates** provide the ability to add maps (which can be
  nested) as template attributes, which you can then deference within the
  template via dot-notation.

**If you're upgrading from Scalasti 2.x**, see the
[Upgrading](#upgrading-from-scalasti-version-2)
section, below.

If you're just looking for the API docs, you can
[jump straight to that section](#api-documentation).

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
Worse, by default, StringTemplate uses Java Bean semantics to access object field values.
While Java Bean semantics are great for Java objects, they're not so good for
Scala objects. With an API that only supports Java Bean semantics, you can only
use Scala objects you can annotate with the Scala `@BeanProperty` annotation.
All other objects (e.g., those from APIs you do not control) must be manually
wrapped in Java Beans.

You can certainly use StringTemplate
[attribute renderers](https://github.com/antlr/stringtemplate4/blob/master/doc/renderers.md)
to address the problem, but you'll need to write an attribute renderer for
every Scala class that can't be coerced into a Java Bean.
 
You can also write a Scala object 
[model adapter](https://github.com/antlr/stringtemplate4/blob/master/doc/adaptors.md),
using the approach outlined 
[here](https://github.com/antlr/stringtemplate4/blob/master/doc/faq/object-models.md).
That approach will work, but it uses runtime reflection on each reference,
which is likely to be slower than the approach Scalasti uses. 

The Scalasti wrapper library attempts to alleviate those pain points. Scalasti 
builds on-the-fly wrappers for Scala objects, using a combination of Java 
`Proxy` objects, (minimal) reflection, and bytecode generation.
If you use Scalasti, you can use StringTemplate with Scala objects and 
collections, without wrapping them yourself and without writing complicated
model adapters.

# Restrictions

Scalasti provides a subset of the functionality in StringTemplate. In the
author's opinion, Scalasti provides the most _useful_ subset, but it does lack
certain features available directly in StringTemplate. For instance:

* Scalasti provides no API for using the StringTemplate compiler.
* Scalasti lacks the equivalent of StringTemplate's `inspect()` methods.
* Scalasti does not expose StringTemplate's event API.
* Scalasti does not expose the StringTemplate interpreter.

You can easily access those (more advanced) StringTemplate features by
dropping down to the StringTemplate API; Scalasti makes it easy for you to
gain access to the underlying wrapped StringTemplate objects. See
[Using Scalasti](#using-scalasti) for further details.

# Installation

Scalasti is published to the
[Bintray Maven repository](https://bintray.com/bmc/maven), which is
automatically linked to Bintray's [JCenter](https://bintray.com/bintray/jcenter)
repository. (From JCenter, it's eventually pushed to the
automatically sync'd with the [Maven Central Repository][].

* Version 3.0.0 supports Scala 2.12, 2.11 and 2.10 
  **and makes radical changes to the API, to support immutability.**
* Versions from 2.1.0 on support Scala 2.12, 2.11 and 2.10.
* Version 2.0.0 (for StringTemplate version 4) supports Scala 2.11 and 2.10.
* Version 1.0.0 (for StringTemplate version 3) supports Scala 2.10.
* Version 0.5.8 (for StringTemplate version 3) supports Scala 2.9.1-1, 2.9.1,
  2.9.0-1, 2.9.0, 2.8.2, 2.8.1 and 2.8.0.

## Installing for Maven

If you're using [Maven][], just specify the artifact, and Maven will do the
rest for you:

* Group ID: `org.clapper`
* Artifact ID: `scalasti_2.10`, `scalasti_2.11`, `scalasti_2.12`
* Version: `3.0.0`
* Type: `jar`

Here's a sample Maven POM "dependency" snippet:

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>scalasti_2.11</artifactId>
      <version>3.0.0</version>
    </dependency>

For more information on using Maven and Scala, see Josh Suereth's
[Scala Maven Guide][].

## Using with SBT

Just add:

    libraryDependencies += "org.clapper" %% "scalasti" % "3.0.0"

# Building from Source

## Source Code Repository

The source code for the Scalasti library is maintained on [GitHub][]. To
clone the repository, run this command:

    git clone git://github.com/bmc/scalasti.git

## Build Requirements

Building the Scalasti library requires [SBT][] 0.13.x or better. 

* If you're on a Mac or Unix system, you can just use the `bin/activator`
  script, supplied with the source code. It'll download the appropriate
  version of SBT (if necessary) and fire it up for you.
* On Windows, you'll have to install SBT, as described at the SBT web site.

## Building Scalasti

If you're using Activator:

```
bin/activator compile test package
```

If you're using SBT directly:

```
sbt compile test package
```

# Runtime Requirements

Scalasti 3 requires the following libraries to be available at runtime, for
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

```scala
val group = STGroupDir("/home/bmc/templates")
val templateTry = group.instanceOf("mytemplate")

// instanceOf returns a scala.util.Try.
t.map { template =>
  template.render()
}
.recover {
  case e: Exception => // handle error
}
```

Create a template group whose contents are in a string, and retrieve a template:

```scala
val templateString =
  """
  |foo(firstName, lastName) ::= <<
  |This is a test template. It spans multiple lines, and it interpolates
  |a first name (<firstName>) and a last name (<lastName>).
  """.stripMargin

val group = STGroupString(templateString)
group.instanceOf("foo").map { template =>
  // do something with the template
}
.recover {
  case e: Exception => // handle error
}
```

Read a template group from a single file, and retrieve a template:

```scala
val group = STGroupFile("/home/bmc/templates/foo.stg")
val template = group.instanceOf("foo")
```

Read a template group from a URL, and retrieve a template:

```scala
import java.net.URL

val group = STGroupFile(new URL("http://localhost/~bmc/templates/foo.stg")
val template = group.instanceOf("foo")
```

Create a template on the fly:

```
val template = ST("Test template. <firstName> <lastName>")
```

Set attribute values one by one. Note that, since `ST` is immutable, each call
to `add()` returns a _new_ `ST` object; the original is unchanged.

```scala

val filledTemplate = template
  .add("firstName", "Moe")                         // single-valued attribute
  .add("lastName", "Howard")                       // single-valued attribute
  .add("employees", Seq("Moe", "Larry", "Curley")) // multi-valued attribute    
    
println(filledTemplate.render())
```

Set attribute values all at once:

```scala
val filledTemplate = template.addAttributes(Map("firstName" -> "Moe", "lastName" -> "Howard"))
```

Change how an attribute (class) is rendered. Note that this does _not_ work
on primitives (i.e., subclasses of `AnyVal`).

```scala
case class HexValue(n: Long)
class HexValueRenderer extends AttributeRenderer[HexValue] {
  def toString(v: HexValue, format: String, locale: Locale) = {
    f"0x${v.n}%x"
  }
}

val memoryLocation: Long = 91234871234987l
val group = STGroupString(
  """|delimiters "$", "$"
     |hex(n) ::= <<$n$>>""".stripMargin
).registerRenderer(new HexValueRenderer)

val template = group.instanceOf("hex")
val filledTemplate = template.add("hex", HexValue(memoryLocation))
```
Render a template with its current set of attributes:

```scala
template.render() match {
  case Failure(ex) => // handle failure
  case Success(str) => println(str)
}
```


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
easy to group data via a concept called _aggregates_. 

In the StringTemplate API, you use the `addAggr()` method to add an automatic 
aggregate. In Scalasti, you use the `ST.addAggregate()` method:

```scala
val st = ST("<page.title>\n\n<page.body>\n")
st.addAggregate("page.{title,body}", title, body)
```

Here's the declaration of `addAggregate()`:

```scala
def addAggregate(aggrSpec: String, values: Any*): ST
```

`addAggregate()` mirrors, almost exactly, what the underlying StringTemplate 
library does. It sets an automatic aggregate from the specified arguments, 
returning a new template. (Because Scalasti's `ST` is immutable, the original 
template remains unchanged.)

An automatic aggregate looks like an object from within a template, but it 
isn't backed by a JavaBean. Instead, there's special logic within StringTemplate
specific to automatic aggregates.

You specify the aggregate with a special syntax. As another example, the
following code defines an aggregate attribute called `name`, with two fields,
`first` and `last`. The values in the brackets will be satisfied from the
remaining arguments to `addAggregate()`. In other words, assuming the template
start and stop characters are both "$", `$name.first$` will be replaced with 
"Jeff", and `$name.last$` will be replaced by "Lebowski".

```scala
val st = ST( ... )
st.setAggregate("name.{first,last}", "Jeff", "Lebowski")
```

**Note**: This syntax does not support nested aggregates. That is, there is no
way, using automatic aggregates, to produce an attribute that can be
interpolated like this:

```
<foo.outer.inner>
```

For that capability, you need Scalasti's _mapped aggregates_. 

### Mapped Aggregates

Scalasti adds another form of aggregate attribute called a "mapped aggregate".
Mapped aggregates are a Scalasti-only feature; they are not supported in the
underlying StringTemplate API.
 
A mapped aggregate is, simply put, a way to create StringTemplate attributes 
from Scala maps. The supplied map's keys are used as the fields of the
aggregate. And mapped aggregation supports nested maps.

The mapped aggregates feature allows you to create a map (or a nested maps)
that you can access with dot-notation within a template. Here's a complete 
example:

```scala
val st = ST("<foo.bar.baz> <foo.x.y.z>")
  .addMappedAggregate("foo", Map("bar" -> Map("baz" -> 1),
                                 "x"   -> Map("y" -> Map("z" -> "hello"))))

```

When rendered, the template will produce the string "1 hello".
When StringTemplate resolves `<foo.bar.baz>`, it'll behave like the following 
code:

```scala
val foo = Map("bar" -> Map("baz" -> 1),
              "x"   -> Map("y" -> Map("z" -> "hello")))
foo("bar")("baz")
```

The supplied map _must_ use string keys; under the covers, the values in the 
map are mapped, recursively, to Java objects, in a similar way as `add()` maps 
values. (See below.)

### Scala Bean Aggregates

Normally, StringTemplate expects non-primitive attributes to be either
Java collections or Java Beans. In Scala, you can force a class to have Java 
Bean getters and setters by marking fields with the `@BeanProperty` annotation.
However, sometimes that's annoying or even impossible. For example, if you
have an instance of a final Scala class from some third party API, you
can't necessarily change that class to add `@BeanProperty` to the fields
you want StringTemplate to see; writing a wrapper class is usually your
only option.

To solve this problem, Scalasti automatically wraps Scala attribute values in
Java Beans, using the [ClassUtil][] library. 

Sometimes, however, you don't want that automatic wrapping. For instance,
you don't want Scalasti to generate a Java Bean wrapper:

* if you're adding a primitive (e.g., an `Int`) value
* if you're adding an object that already supports Java Bean semantics
* if you're adding a value for which you have registered an
  `AttributeRenderer`

In the first two cases, the extra wrapping is pointless.

In the `AttributeRenderer` case, it would actually break the renderer lookup.
With an `AttributeRenderer`, StringTemplate matches the values to the
attributes by matching classes; the automatic Bean-wrapping breaks that
matching.

In these cases, you simply add _raw_ attributes to a template, by
specifying a special third `raw` parameter to the `add()` methods. 
The parameter defaults to `false`.

```scala
st.add("foo", foo)           // foo gets wrapped in a generated Java Bean
st.add("bar", bar, raw=true) // bar gets added without wrapping
```

**NOTE**: The wrapping capability requires the presence of the ASM byte code
generation library at runtime.

## Access to Underlying `StringTemplate`

At any point, you can retrieve the underlying [StringTemplate][] API's
`StringTemplate` object, via a call to the `nativeTemplate` method:

```scala
val st = ST(template).add("thing", thing).add("foo", foo)
val nativeTemplate = st.nativeTemplate
```

Once you have the native template, you can interact with it using the
methods it exposes (i.e., the [StringTemplate][] Java API methods).

**WARNING**: Scalasti presents an _immutable_ view of the StringTemplate API.
Calling Scalasti update functions (like `ST.add()`, `ST.set()`,
`ST.addAttributes()`, etc.) copy both the Scalasti object _and_ the underlying
StringTemplate object it wraps. Dropping down to the native StringTemplate,
while supported, bypasses all immutability protections. It also means you're
now interacting with the StringTemplate library, which expects objects with
Java semantics, not Scala semantics.

## Upgrading from Scalasti version 2

As of version 3.0.0, Scalasti objects are _immutable_. Prior to 3.0.0,
Scalasti offered a mutable interface, consistent with StringTemplate.
The newly immutable API works differently than the 2.x API.

Here are some of the specific differences:

### Mutator methods return new objects

In Scalasti 2.x, you could create a template and then add attributes to it,
as shown here:

```scala
val st = ST("...")
st.add("foo", 10)
st.add("bar", "This is awesome.")
st.render()
```

In Scalasti 3.x, that won't work, because each call to `add()` returns a
_new_ object; the object on which you call `add()` remains unchanged. The
long form solution looks something like this:

```scala
val st = ST("...")
val st2 = st.add("foo", 10)
val st3 = st2.add("bar", "This is awesome.")
st3.render()
```

Of course, chaining the calls, where your logic permits, is much cleaner:

```scala
val st = ST("...").add("foo", 10).add("bar", "This is awesome.")
st.render()
```

### Methods no longer throw exceptions

Any method that _could_ fail now returns a `scala.util.Try`. There are
several such methods, but the most common is `ST.render()`.

In Scalasti 2.x, you could do this:

```scala
val st = ST("...").add("foo", 10).add("bar", "This is awesome.")
println(st.render()) // ST.render() returned a String in 2.x
```

If the `render()` call failed—for instance, because it couldn't find a
referenced attribute—it would simply throw an exception.

In Scalasti 3.x, however, `render()` returns a `Try[String]`. If the
render operation works, you'll get back a `Success` object that contains
the rendered template. If it fails, you'll get a `Failure` object that
contains an exception.

There are several ways to handle this return result, shown in the snippet
below:

```scala
// Ugh
val t = st.render()
if (t.isSuccess) {
   println(t.get)
}
else {
   // handle failure, which will look kind of ugly, no matter how you do it
}

// Pattern matching is a little better.
st.render() match {
  case Failure(ex) => // handle failure
  case Success(str) => println(str)
}

// And, of course, you can map over the `Try`
import scala.util.control.NonFatal
st.render().map { str =>
  println(str)
}
.recover {
  case NonFatal(ex) => // handle failure
}
```

Also, because `render()` returns a `Try`, and `Try` supports `map()`,
`flatMap()`, and `withFilter()`, you can use it in a `for` comprehension.

### No more listeners

StringTemplate has this notion of listeners: With the `STGroup`-derived
classes, you can register a listener that will be notified of various
types of processing errors via callbacks.

Because Scalasti now returns results as `Try` objects, when a failure could
occur, this listener interface isn't necesary. And, besides, it's not
exactly idiomatic Scala.

Under the covers, Scalasti now registers its own listeners that throw
exceptions on errors; Scalasti then catches any such exceptions and returns
them in a `Failure` object. This approach is much cleaner and much more
functional.

## API Documentation

For full details, see the API documentation:

* [Current version (3.x)](api/3.x):
* [Scalasti 2.x](api/2.x)

## Additional Documentation

Consult the [StringTemplate documentation][] for complete details on creating,
deploying, and using StringTemplate templates.

# Author

[Brian M. Clapper][]

# Contributing to Scalasti

If you have suggestions or contributions, feel free to fork the
[Scalasti repository][repo], make your changes, and send me a pull request.

# Copyright and License

Scalasti is copyright &copy; 2010-2017 Brian M. Clapper and is released
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

---
title: Scalasti, a Scala interface to the Java StringTemplate library
layout: withTOC
---

## Introduction

The Scalasti library is a [Scala][] interface to the [StringTemplate][]
Java template library. It provides a subset of the features of
[StringTemplate][], using a more Scala-friendly syntax.

## Rationale

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

## Installation

The simplest way to install the Scalasti library is to download a
pre-compiled jar from the [*clapper.org* Maven repository][]. You can get
certain build tools to do the heavy lifting for you.

### Installing for Maven

If you're using [Maven][], you can get the Scalasti library from the
[*clapper.org* Maven Repository][]. The relevant pieces of information are:

* Group ID: `clapper.org`
* Artifact ID: `scalasti_`*scala-version*
* Version: `0.3`
* Type: `jar`
* Repository: `http://maven.clapper.org/`

Substitute either "2.8.0.RC3" or "2.8.0.RC2" for *scala-version*. For example:

    <dependency>
      <groupId>org.clapper</groupId>
      <artifactId>scalasti_2.8.0.RC3</artifactId>
      <version>0.3</version>
    </dependency>

### Using with SBT

If you're using [SBT][] to build your code, place the following lines in
your project file (i.e., the Scala file in your `project/build/`
directory):

    val clapperOrgRepo = "clapper.org Maven Repository" at "http://maven.clapper.org"
    val scalasti = "org.clapper" %% "scalasti" % "0.3"

**NOTE:** The first doubled percent is *not* a typo. It tells SBT to treat
Scalasti as a cross-built library and automatically inserts the Scala
version you're using into the artifact ID. It will *only* work if you are
building with Scala 2.8.0.RC3 or Scala 2.8.0.RC2. See the
[SBT cross-building][] page for details.

## Building from Source

### Source Code Repository

The source code for the Scalasti library is maintained on [GitHub][]. To
clone the repository, run this command:

    git clone git://github.com/bmc/scalasti.git

### Build Requirements

Building the Scalasti library requires [SBT][]. Install SBT, as described
at the SBT web site.

### Building Scalasti

Assuming you have an `sbt` shell script (or .BAT file, for *\[shudder\]*
Windows), first run:

    sbt update

That command will pull down the external jars on which the Scalasti library
depends. After that step, build the library with:

    sbt compile test package

The resulting jar file will be in the top-level `target` directory.

## Using Scalasti

The Scalasti API provides simple wrappers around the most common classes in
the [StringTemplate][] API. For various reasons, subclassing the
StringTemplate classes is non-trivial, so Scalasti's classes are wrappers
that delegate their operations to the wrapped StringTemplate object. Since
Scalasti does not provide the full suite of capabilities available in the
actual StringTemplate classes, you can, at any point, retrieve a copy of
the actual underlying StringTemplate API object, so you can interact
directly with it.

### Simple Examples

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
    class HexValueRenderer extends AttributeRenderer[HexValue]
    {
        def toString(v: HexValue) = "0x" + v.toHexString
    }

    val memoryLocation: Long = ...
    template.setAttribute("hexidecimal address", new HexValue(memoryLocation))
    template.setAttribute("decimal address", memoryLocation)
    template.registerRenderer(new HexValueRenderer)

Render a template with its current set of attributes:

    println(template.toString)

### API Documentation

The Scaladoc-generated the [API documentation][] is available locally.
In addition, you can generate your own version with:

    sbt doc

### Additional Documentation

Consult the [StringTemplate documentation][] for complete details on creating,
deploying, and using StringTemplate templates.

## Author

[Brian M. Clapper][]

## Contributing to Scalasti

Scalasti is still under development. If you have suggestions or
contributions, feel free to fork the [Scalasti repository][], make your
changes, and send me a pull request.

## Copyright and License

Scalasti is copyright &copy; 2010 Brian M. Clapper and is released under a
[BSD License][].

## Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [Scalasti repository][] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the Scalasti project
  under a [BSD License][].

[API documentation]: api/
[BSD License]: license.html
[Scala]: http://www.scala-lang.org/
[StringTemplate]: http://www.stringtemplate.org/
[StringTemplate documentation]: http://www.antlr.org/wiki/display/ST/StringTemplate+Documentation
[Scalasti]: http://bmc.github.com/scalasti
[Scalasti home page]: http://bmc.github.com/scalasti
[Scalasti repository]: http://github.com/bmc/scalasti
[Brian M. Clapper]: mailto:bmc@clapper.org
[Google's Closure Templates]: http://code.google.com/closure/templates/index.html
[Velocity]: http://velocity.apache.org/
[FreeMarker]: http://www.freemarker.org/
[Scalate]: http://scalate.fusesource.org/
[*Enforcing Strict Model-View Separation in Template Engines*]: http://www.cs.usfca.edu/~parrt/papers/mvc.templates.pdf
[SBT]: http://code.google.com/p/simple-build-tool
[*clapper.org* Maven repository]: http://maven.clapper.org/org/clapper/
[Maven]: http://maven.apache.org/
[GitHub repository]: http://github.com/bmc/scalasti
[GitHub]: http://github.com/bmc/
[SBT cross-building]: http://code.google.com/p/simple-build-tool/wiki/CrossBuild
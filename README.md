Scalasti: A Scala interface to the Java StringTemplate Library
==============================================================

[![Build Status](https://travis-ci.org/bmc/scalasti.svg?branch=master)](https://travis-ci.org/bmc/scalasti)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.clapper/scalasti_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.clapper/grizzled-scala_2.11)

## Introduction

This is Scalasti, a [Scala][] interface to the [StringTemplate][] Java template
library.

Scalasti provides a (useful) subset of the StringTemplate API's
features, with a more Scala-friendly syntax.

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

For complete information, see the [Scalasti home page][].

## Copyright and license

[Scalasti][] is copyright &copy; 2010-2018 [Brian M. Clapper][] and is
released under a BSD license. See the accompanying license file for
details.

[Scala]: http://www.scala-lang.org/
[StringTemplate]: http://www.stringtemplate.org/
[Scalasti home page]: http://software.clapper.org/scalasti
[Scalasti]: http://software.clapper.org/scalasti
[Brian M. Clapper]: mailto:bmc@clapper.org


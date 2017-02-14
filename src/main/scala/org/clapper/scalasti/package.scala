package org.clapper

/** Scalasti is a Scala library that wraps the Java-based
  * [[http://www.stringtemplate.org StringTemplate]] template library.
  * Scalasti provides a (useful) subset of the StringTemplate API's
  * features, with a more Scala-friendly syntax.
  *
  * Scalasti's additional features include:
  *
  *  - '''Immutability'''. Scalasti objects are immutable, unlike the
  *    StringTemplate objects. Modifier methods always create new objects;
  *    they never modify objects in place.
  *
  *  - '''Error-handling'''. Where possible, Scalasti propagates errors
  *    via `scala.util.Try` objects, instead of via a StringTemplate
  *    listener.
  *
  *  - '''Scala object support'''. Scalasti supports Scala objects, meaning
  *    you don't have to use `@BeanProperty` on your Scala classes before you
  *    can pass them into a template. This feature also allows you to use
  *    instances of third-party Scala classes directly with Scalasti.
  *
  *  - '''Stronger type safety'''. You should ''never'' need to cast objects
  *    you receive from the Scalast API.
  *
  * For complete details, see the
  * [[http://software.clapper.org/scalasti/ Scalasti home page]].
  */
package object scalasti


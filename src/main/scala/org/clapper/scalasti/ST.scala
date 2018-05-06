/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010-2018 Brian M. Clapper
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

   * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

   * Neither the names "clapper.org" nor the names of its contributors may
    be used to endorse or promote products derived from this software
    without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ---------------------------------------------------------------------------
*/

package org.clapper.scalasti

import org.clapper.classutil.MapToBean

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.runtimeMirror
import org.stringtemplate.v4.{STWriter, ST => _ST}
import java.util.Locale

import scala.util.Try
import TypeAliases._
import org.stringtemplate.v4.misc.ErrorManager

/** A Scala interface to a StringTemplate `ST` template. Note that this
  * interface does not directly expose all the underlying ST methods. In
  * particular, this Scala interface is geared primarily toward reading and
  * rendering external templates, not toward generating templates in code.
  *
  * This class cannot be instantiated directly. Instead, use the `apply()`
  * methods in the companion object.
  *
  * Because of the way the ST API instantiates templates, this class cannot
  * easily subclass the real ST class. So, it wraps the underlying string
  * template object and stores it internally. You can retrieve the wrapped
  * template object via the `[[nativeTemplate]]` method.
  *
  * '''WARNING''': This API presents an ''immutable'' view of the
  * StringTemplate API. Calling update functions (like [[add]](),
  * [[set]](), [[addAttributes]](), etc.) copy both the Scalasti object
  * ''and'' the underlying StringTemplate object it wraps. Dropping down to
  * the native StringTemplate, while supported, bypasses all immutability
  * protections. It also means you're now interacting with the StringTemplate
  * library, which expects objects with Java semantics, not Scala semantics.
  *
  * Note that this class explicitly handles mapping the following types of
  * values in an attribute map:
  *
  *  - A Scala `Seq` (which includes lists and array buffers) is mapped to
  *    a `java.util.List`, so it's treated as a multivalued attribute by the
  *    underlying ST library.
  *  - A Scala iterator is also mapped to a `java.util.List`.
  *  - Numbers and strings are added as is.
  *  - Anything else is treated as a regular object and wrapped in a Java
  *    Bean. See below.
  *
  * == Bean Wrapping ==
  *
  * Regular objects are, by default, wrapped in a Java Bean, because the
  * underlying String Template API uses Java Bean semantics to access object
  * fields. Thus, if a template references "foo.bar", StringTemplate will expect
  * that the object associated with the name "foo" has a method called
  * `getBar()`. To allow Scala objects (and, especially, case class objects)
  * to be used directly, Scalasti automatically generates wrapper Java Beans
  * for them.
  *
  * There are cases where you don't want this behavior, however. For instance,
  * it doesn't make much sense with numeric values or strings, so Scalasti
  * deliberately does not wrap those objects. There are other cases where
  * you might not want the automatic Bean-wrapper behavior; see the
  * `[[add]]` method for more details.
  */
case class ST private[scalasti] (private[scalasti] val native: _ST,
                                 private[scalasti] val template: String,
                                 private[scalasti] val attributeMap: AttrMap) {

  makeErrorsExceptions(native)

  /** The delimiter start character for template substitutions. Corresponds
    * to `delimiterStartChar` in the underlying StringTemplate API.
    */
  val startDelimiter = native.groupThatCreatedThisInstance.delimiterStartChar

  /** The delimiter stop character for template substitutions. Corresponds
    * to `delimiterStopChar` in the underlying StringTemplate API.
    */
  val endDelimiter = native.groupThatCreatedThisInstance.delimiterStopChar

  /** Add an attribute to the template. Sequences, iterators and maps are
    * converted (copied) to their Java equivalents, with their contents
    * mapped to strings. (If you need nested objects of arbitrary depth,
    * used a mapped aggregate.) Other objects are wrapped in Java Beans,
    * unless `raw` is set to `true`.
    *
    * '''NOTE''': If you add an `[[AttributeRenderer]]` to a parent group,
    * any objects you intend the renderer to render ''must'' be added as
    * raw objects. The underlying StringTemplate API matches attribute
    * renders to attributes by type (i.e., by Java class). If you don't
    * specify `raw=true`, Scalasti will wrap the object in a generated
    * Java Bean, and StringTemplate won't be able to match it with the
    * corresponding renderer.
    *
    * Adding an attribute twice seems to add it twice, causing it to be
    * rendered concatenated, in some cases. If you want to ensure that it
    * is set "clean", use the `[[set]]` method, which clears any existing
    * attribute first.
    *
    * @param name   the name to associate with the attribute. This is the name
    *               by which the attribute can be referenced in the template
    * @param value  the value of the attribute. The value must not be null.
    *               Use `None`, if that's what you want.
    * @param raw    `false` (the default) to wrap the value in a Java Bean.
    *               `true` to add it, as is.
    *
    * @return a new, updated object. This object remains unchanged.
    */
  def add(name: String, value: Any, raw: Boolean = false): ST = {
    require(value != null)
    val newAttrs = this.attributeMap + (name -> Attribute(value, raw))
    this.copy(native = newUnderlying(newAttrs), attributeMap = newAttrs)
  }

  /** Set an attribute in the template, clearing any existing attribute of
    * the same name first. Calling this method is the same as calling
    * `[[remove]]`, followed by `[[add]]`.
    *
    * @param name   the name to associate with the attribute. This is the name
    *               by which the attribute can be referenced in the template
    * @param value  the value of the attribute. The value must not be null.
    *               Use `None`, if that's what you want.
    * @param raw    `false` (the default) to wrap the value in a Java Bean.
    *               `true` to add it, as is.
    *
    * @return a new, updated object. This object remains unchanged.
    */
  def set(name: String, value: Any, raw: Boolean = false): ST = {
    require(value != null)
    val newAttrs = (this.attributeMap - name) + (name -> Attribute(value, raw))
    this.copy(native = newUnderlying(newAttrs), attributeMap = newAttrs)
  }

  /** Add a map of objects (key=value pairs) to the template.
    *
    * @param attrs  the attributes to add. The map must contain no null values.
    *               Use `None` if you want to signify null.
    * @param raw    `false` (the default) to wrap all values in Java Beans.
    *               `true` to add it, as is.
    *
    * @return a new, updated object. This object remains unchanged.
    */
  def addAttributes(attrs: Map[String, Any], raw: Boolean = true): ST = {
    require(! attrs.values.exists(_ == null))
    val newAttrs = this.attributeMap ++ attributeMapToInternal(attrs, raw)
    this.copy(native = newUnderlying(newAttrs), attributeMap = newAttrs)
  }

  /** Clear all existing attributes from the template, and add a new map
    * of objects (key=value pairs) to the template.
    *
    * @param attrs  the attributes to set. The map must contain no null values.
    *               Use `None` if you want to signify null.
    * @param raw    `false` (the default) to wrap all values in Java Beans.
    *               `true` to add it, as is.
    *
    * @return a new, updated object. This object remains unchanged.
    */
  def setAttributes(attrs: Map[String, Any], raw: Boolean = false): ST = {
    require(! attrs.values.exists(_ == null))
    this.copy(native = newUnderlying(attributeMapToInternal(attrs, raw)))
  }

  /** Remove an attribute from the template.
    *
    * @param name  the name of the attribute to remove
    *
    * @return a new object with the specified attribute removed
    */
  def remove(name: String): ST = {
    if (this.attributeMap contains name) {
      val newAttrs = attributeMap - name
      this.copy(native = newUnderlying(newAttrs), attributeMap = newAttrs)
    }
    else {
      this
    }
  }

  /** Retrieve an attribute from the map.
    *
    * @param name  the name of the attribute to retrieve
    *
    * @tparam T    the attribute's expected type
    *
    * @return `Some(result)` if found, or `None` if not.
    */
  def attribute[T: ru.TypeTag](name: String): Option[T] = {
    def mapClass(v: Any): Class[_] = {
      // We're matching against boxed types for primitives, since Scala's
      // reflection returns boxed types for Java primitives. But, Java's
      // getClass() returns primitives for primitives. So, we have to map
      // them.
      v match {
        case _: Int     => classOf[Int]
        case _: Float   => classOf[Float]
        case _: Double  => classOf[Double]
        case _: Boolean => classOf[Boolean]
        case _: AnyRef  => v.getClass
      }
    }

    attributeMap.get(name).flatMap { attr =>
      val value = attr.value match {
        case Some(v) => Some(v)
        case None    => None
        case v       => Some(v)
      }

      value.flatMap { v =>
        val tpe         = ru.typeTag[T].tpe
        val classLoader = this.getClass.getClassLoader
        val cls         = runtimeMirror(classLoader).runtimeClass(tpe)
        val vClass      = mapClass(v)

        v match {
          case None => None
          case _    => if (cls.isAssignableFrom(vClass))
                         Some(v.asInstanceOf[T])
                       else
                         None
        }
      }
    }
  }

  /** Get a map of the attributes in the template.
    *
    * @return the internal attributes
    */
  def attributes: Map[String, Any] = attributeMap

  /** Get the template's name.
    *
    * @return  the name, which will always be non-null
    */
  def name: String = native.getName

  /** Determine whether this template is an anonymous sub-template. See
    * the StringTemplate documents for details.
    *
    * @return `true` if the template is an anonymous subtemplate, `false`
    *         otherwise
    */
  def isAnonSubtemplate: Boolean = native.isAnonSubtemplate

  /** Write the template to an `STWriter`. The `STWriter` interface is
    * supplied by the underlying StringTemplate API, which also supplies
    * some implementations of it (e.g., `AutoIndentWriter` and
    * `NoIndentWriter`).
    *
    * @param out       the `STWriter`
    * @param locale    the locale
    *
    * @return `Success(total)`, with the total number of characters written;
    *         or `Failure(exception)` on error.
    */
  def write(out: STWriter, locale: Locale = Locale.getDefault): Try[Int] = {
    Try {
      native.write(out, locale)
    }
  }

  /** Render the template to a string.
    *
    * @param locale    The locale to use
    * @param lineWidth The line width
    *
    * @return A `Success` containing the rendered string, or a `Failure`
    *         on error.
    */
  def render(locale:    Locale = Locale.getDefault,
             lineWidth: Int = 80): Try[String] = {
    Try {
      native.render(locale, lineWidth)
    }
  }

  /** Set an automatic aggregate from the specified arguments. An
    * automatic aggregate looks like an object from within a template, but
    * it isn't backed by a bean. Instead, you specify the aggregate with a
    * special syntax. For instance, the following code defines an
    * aggregate attribute called `name`, with two fields, `first` and
    * `last`. Those fields can be interpolated within a template via
    * `\$item.first$` and `\$item.last$`.
    *
    * {{{
    * val st = new ST( ... )
    * st.setAggregate("name.{first,last}", "Moe", "Howard")
    * }}}
    *
    * Setting the same aggregate multiple times results in a list of
    * aggregates:
    *
    * {{{
    * val st = new ST( ... )
    * st.setAggregate("name.{first,last}", "Moe", "Howard")
    * st.setAggregate("name.{first,last}", "Larry", "Fine")
    * st.setAggregate("name.{first,last}", "Curley", "Howard")
    * }}}
    *
    * Note, however, that this syntax does not support nested aggregates.
    * Use `addMappedAggregate()` for that.
    *
    * See
    * [[http://www.antlr.org/wiki/display/ST/Expressions#Expressions-Automaticaggregatecreation]]
    * for more information.
    *
    * @param aggrSpec  the spec, as described above
    * @param values    one or more values. The values are treated as discrete;
    *                  that is, lists are not supported.
    *
    * @return a new object, with the new aggregate
    */
  def addAggregate(aggrSpec: String, values: Any*): ST = {
    val valuesAsObjects = values.map(v => ScalaToJava.anyToJava(v))
    val newNative = newUnderlying(this.attributeMap)
    newNative.addAggr(aggrSpec, valuesAsObjects.toArray: _*)
    this.copy(native = newNative)
  }

  /** Create a "mapped aggregate". The supplied map's keys are used as the
    * fields of the aggregate. With a mapped aggregate, Scalasti actually
    * translates the map into a Java Bean, which it then uses to set the
    * attribute. Because Scalasti recursively converts all maps it finds
    * (as long as they are of type `Map[String, Any]`), a mapped attribute
    * can handle nested attribute references.
    *
    * The underlying ST library does _not_ support the notion
    * of a mapped aggregate; mapped aggregates are a Scalasti add-on.
    *
    * For example, given this map:
    *
    * {{{
    * Map("foo" -> List(1, 2), "bar" -> "barski")
    * }}}
    *
    * and the name "mystuff", this method will produce the equivalent of the
    * following call:
    *
    * {{{
    * template.setAggregate("mystuff.{foo, bar}", List(1, 2), "barski")
    * }}}
    *
    * Nested maps are supported. For instance, this code fragment:
    *
    * {{{
    * val attrMap = Map("foo"   -> "FOO",
    *                   "alien" -> Map("firstName" -> "John",
    *                                  "lastName"  -> "Smallberries"))
    * template.setAggregate("thing", attrMap)
    * }}}
    *
    * will make the following values available in a template:
    *
    * {{{
    * \$thing.foo$                  # expands to "FOO"
    * \$things.alien.firstName$     # expands to "John"
    * \$things.alien.lastName$      # expands to "Smallberries"
    * }}}
    *
    * @param attrName  the attribute's name (i.e., the outermost name)
    * @param valueMap  the map of attribute fields
    *
    * @return a new object, if the value map has something in it; this object,
    *         if not.
    */
  def addMappedAggregate(attrName: String, valueMap: Map[String, Any]): ST = {
    if (valueMap.nonEmpty)
      add(attrName, MapToBean(valueMap))
    else
      this
  }

  /** Get the original template.
    *
    * @return the original template
    */
  def originalTemplate: String = template

  /** Get the underlying native StringTemplate API template.
    *
    * @return the template
    */
  def nativeTemplate: _ST = native

  /** Return a string representation of the template. '''NOTE''': As of
    * StringTemplate 4, the `toString()` method no longer renders the
    * template. Use the `[[render]]` method for that.
    *
    * @return the rendered template.
    */
  override def toString: String = native.toString

  // ----------------------------------------------------------------------
  // Private Methods
  // ----------------------------------------------------------------------

  /** Convert a caller-supplied attribute map to an internal attribute map.
    *
    * @param attrs  the map of attributes
    * @param raw    the raw flag to apply to all values
    *
    * @return the internal map
    */
  private def attributeMapToInternal(attrs: Map[String, Any],
                                     raw:   Boolean): AttrMap = {
    attrs.map { case (name, value) => name -> Attribute(value, raw) }
  }

  /** Ensure that any errors that occur during template processing result
    * in a thrown exception that can be captured in a `Try`.
    *
    * @param underlying the native `STGroup` object whose error handling is to
    *                   be changed
    */
  private def makeErrorsExceptions(underlying: _ST): Unit = {
    underlying.groupThatCreatedThisInstance.errMgr =
      new ErrorManager(new ThrowExceptionErrorListener)
  }

  /** Create a new underlying `StringTemplate.ST` object, with the same
    * parameters is the one in this object.
    *
    * @param attrMap an attribute map. Defaults to this object's map. The
    *                map must already be mapped to Java-compatible objects.
    */
  private def newUnderlying(attrMap: AttrMap = this.attributeMap) = {
    // It's absolutely critical that the parent group (i.e., the group
    // inside the existing underlying) be copied into the new template.
    // There is a copy constructor, but it doesn't appear to work properly.
    // This is manual, and it appears to work, for now.
    val st = new _ST(native.groupThatCreatedThisInstance, template)
    //val st = new _ST(native)
    makeErrorsExceptions(st)
    applyAttributes(st, attrMap)
  }

  /** Apply the attributes in the specified attribute map a (presumably new)
    * underlying ST.
    *
    * @param underlying  the underlying ST
    * @param attrMap     the attribute map
    *
    * @return `underlying`, for convenience
    */
  private def applyAttributes(underlying: _ST, attrMap: AttrMap): _ST = {
    for ((k, attr) <- attrMap) {
      underlying.add(k, ScalaToJava.anyToJava(attr.value, attr.raw))
    }

    underlying
  }
}

/** Companion object for the `ST` class. This object provides `apply()`
  * methods for instantiating `ST` objects.
  */
object ST {
  /** Create an `ST` object that parses and holds the specified template
    * string.
    *
    * @param template            the template string
    * @param startDelimiter  the starting delimiter character
    * @param endDelimiter   the ending delimiter character
    *
    * @return the template
    */
  def apply(template:           String,
            startDelimiter:     Char = Constants.DefaultStartChar,
            endDelimiter:       Char = Constants.DefaultStopChar): ST = {
    new ST(template           = template,
           attributeMap       = EmptyAttrMap,
           native             = new _ST(template,
                                        startDelimiter,
                                        endDelimiter))
  }
}

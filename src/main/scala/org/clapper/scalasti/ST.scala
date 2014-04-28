/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010, Brian M. Clapper
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

import org.clapper.classutil.{MapToBean, ScalaObjectToBean}

import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.runtimeMirror

import org.stringtemplate.v4.{ST => _ST, STWriter, STGroup => _STGroup}

import java.util.{List      => JList,
                  Map       => JMap,
                  HashMap   => JHashMap,
                  ArrayList => JArrayList,
                  Locale}

/** A Scala interface to a StringTemplate `ST` template. objet Note that this
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
  * template object via the `nativeST` method. You are free to call methods
  * directly on `template`, though they will use Java semantics, rather than
  * Scala semantics.
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
  * [[add]] method for more details.
  */
class ST private[scalasti] (private val native: _ST) {

  private val attributeMap = MutableMap.empty[String, Any]

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
    * @param value  the value of the attribute
    * @param raw    `false` (the default) to wrap the value in a Java Bean.
    *               `true` to add it, as is.
    *
    * @return this object, for chaining
    */
  def add(name: String, value: Any, raw: Boolean = false): ST = {
    val v = value match {
      case seq: Seq[_]      => seqToJava(seq)
      case it:  Iterator[_] => iterToJava(it)
      case map: Map[_, _]   => mapToJava(map.asInstanceOf[Map[String, Any]])
      case s: String        => value
      case n: Number        => value
      case o: Any           => if (raw) value else ScalaObjectToBean(value)
    }

    attributeMap += name -> v
    native.add(name, v)
    this
  }

  /** Set an attribute in the template, clearing any existing attribute of
    * the same name first. Calling this method is the same as calling
    * `[[remove]]`, followed by `[[add]]`.
    *
    * @param name   the name to associate with the attribute. This is the name
    *               by which the attribute can be referenced in the template
    * @param value  the value of the attribute
    * @param raw    `false` (the default) to wrap the value in a Java Bean.
    *               `true` to add it, as is.
    *
    * @return this object, for chaining
    */
  def set(name: String, value: Any, raw: Boolean = false): ST = {
    remove(name)
    add(name, value, raw)
  }

  /** Add a map of objects (key=value pairs) to the template.
    *
    * @param attrs  the attributes to add
    * @param raw    `false` (the default) to wrap all values in Java Beans.
    *               `true` to add it, as is.
    *
    * @return this object, for chaining
    */
  def addAttributes(attrs: Map[String, Any], raw: Boolean = true): ST = {
    for ((name, value) <- attrs) {
      add(name, value, raw)
    }

    this
  }

  /** Clear all existing attributes from the template, and add a new map
    * of objects (key=value pairs) to the template.
    *
    * @param attrs  the attributes to add
    * @param raw    `false` (the default) to wrap all values in Java Beans.
    *               `true` to add it, as is.
    *
    * @return this object, for chaining
    */
  def setAttributes(attrs: Map[String, Any], raw: Boolean = true): ST = {
    attributeMap.clear()
    addAttributes(attrs, raw)
  }

  /** Remove an attribute from the template.
    *
    * @param name  the name of the attribute to remove
    */
  def remove(name: String): Unit = {
    native.remove(name)
    attributeMap.remove(name)
  }

  /** Retrieve an attribute from the map.
    *
    * @param name
    * @tparam T
    * @return
    */
  def attribute[T: ru.TypeTag](name: String): Option[T] = {
    def mapClass(v: Any): Class[_] = {
      // We're matching against boxed types for primitives, since Scala's
      // reflection returns boxed types for Java primitives. But, Java's
      // getClass() returns primitives for primitives. So, we have to map
      // them.
      v match {
        case i: Int     => classOf[Int]
        case f: Float   => classOf[Float]
        case d: Double  => classOf[Double]
        case b: Boolean => classOf[Boolean]
        case _: AnyRef  => v.getClass
      }
    }

    attributeMap.get(name).flatMap { v =>
      val tpe         = ru.typeTag[T].tpe
      val classLoader = this.getClass.getClassLoader
      val cls         = runtimeMirror(classLoader).runtimeClass(tpe)
      val vClass      = mapClass(v)

      if (cls.isAssignableFrom(vClass))
        Some(v.asInstanceOf[T])
      else
        None
    }
  }

  def attributes: Map[String, Any] = attributeMap.toMap

  def name = native.getName

  def isAnonSubtemplate: Boolean = native.isAnonSubtemplate

  def write(out:      STWriter,
            locale:   Locale = Locale.getDefault,
            listener: Option[STErrorListener] = None): Either[String, Int] = {
    try {
      val res = listener.map { l => native.write(out, locale, l) }
                        .getOrElse { native.write(out, locale) }
      Right(res)
    }

    catch {
      case e: Throwable => Left(e.getMessage)
    }
  }

  def render(locale: Locale = Locale.getDefault, lineWidth: Int = 80): String = {
    native.render(locale, lineWidth)
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
    * @return this object, for convenience
    */
  def addAggregate(aggrSpec: String, values: Any*): ST = {
    val valuesAsObjects = values.map(transform(_))
    native.addAggr(aggrSpec, valuesAsObjects.toArray: _*)
    this
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
    * @return this object, for convenience
    */
  def addMappedAggregate(attrName: String,
                         valueMap: Map[String, Any]): ST = {
    if (! valueMap.isEmpty)
      add(attrName, MapToBean(valueMap))
    this
  }

  /** Render the template with the current attributes.
    *
    * @return the rendered template.
    */
  override def toString = native.toString

  // ----------------------------------------------------------------------
  // Private Methods
  // ----------------------------------------------------------------------

  /** Transform a value for use in a template.
    *
    * @param v  the value
    *
    * @return a Java object, suitable for use in a template
    */
  private def transform(v: Any) = {
    val v2  = v match {
      case l: List[_]     => seqToJava(l)
      case s: Seq[_]      => seqToJava(s.toList)
      case i: Iterator[_] => seqToJava(i.toList)
      case _              => v
    }

    v2.asInstanceOf[Object]
  }

  /** Maps a Scala map of attributes into a Java map of attributes. The
    * Scala map is converted to a `java.util.HashMap`. The keys are
    * assumed to be strings. The values are mapped as follows:
    *
    * - A Scala `Seq` (which includes lists and array buffers) is mapped to
    *   a `java.util.List`, so it's treated as a multivalued attribute by the
    *   underlying ST library.
    * - A Scala iterator is also mapped to a `java.util.List`.
    * - Anything else is treated as a single-valued object.
    *
    * To enhance how these mappings are done, override this method.
    *
    * @param map  The Scala map to convert.
    *
    * @return the Java map
    */
  private def mapToJava(map: Map[String, Any]): JMap[String, Object] = {
    val result = new JHashMap[String, Object]

    map.foreach(kv => result.put(kv._1, transform(kv._2)))
    result
  }

  /** Convert a Scala sequence to a Java list. All elements are converted
    * to strings.
    *
    * @param seq  the sequence
    *
    * @return the list
    */
  private def seqToJava(seq: Seq[Any]): JList[String] = {
    val list = new JArrayList[String]
    seq.foreach(a => list.add(a.toString))
    list
  }

  /** Convert a Scala iterator to a Java list. All elements are converted
    * to strings.
    *
    * @param it  the iterator
    *
    * @return the Java equivalent
    */
  private def iterToJava(it: Iterator[Any]): JList[String] = {
    seqToJava(it.toSeq)
  }
}

object ST {
  def apply(template:           String,
            delimiterStartChar: Char = '<',
            delimiterStopChar:  Char = '>'): ST = {
    new ST(new _ST(template, delimiterStartChar, delimiterStopChar))
  }

  def apply(group: STGroup, template: String): ST = {
    new ST(new _ST(group.native, template))
  }
}

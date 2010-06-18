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

import org.clapper.scalasti.adapter.ScalastiStringTemplate

import org.antlr.stringtemplate.{StringTemplateGroup => ST_StringTemplateGroup,
                                 StringTemplate => ST_StringTemplate}

import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.Manifest

import java.io.File
import java.util.{List => JList, 
                  Map => JMap, 
                  HashMap => JHashMap,
                  ArrayList => JArrayList}

/**
 * A Scala interface to a StringTemplate template. Note that this interface
 * does not directly expose all the underlying StringTemplate methods. In
 * particular, this Scala interface is geared primarily toward reading and
 * rendering external templates, not toward generating templates in code.
 *
 * Because of the way the StringTemplate API instantiates templates, this
 * class cannot easily subclass the real StringTemplate class. So, it wraps
 * the underlying string template object and stores the value in the public
 * `template` variable. You are free to call methods directly on `template`,
 * though they will use Java semantics, rather than Scala semantics.
 *
 * Note that this class explicitly handles mapping the following types of
 * values in an attribute map:
 *
 * A Scala `Seq` (which includes lists and array buffers) is mapped to
 * a `java.util.List`, so it's treated as a multivalued attribute by the
 * underlying StringTemplate library. A Scala iterator is also mapped to a
 * `java.util.List`. Anything else is treated as a single-valued object.
 *
 * @param group     the StringTemplateGroup in which the template resides
 * @param template  the real, underlying String Template
 */
class StringTemplate(val group: Option[StringTemplateGroup],
                     private val template: ScalastiStringTemplate)
{
    private val attributeMap = MutableMap.empty[String, Any]

    /**
     * Alternate constructor that takes an unnamed, ungrouped template, as
     * contained in a string.
     *
     * @param template  the contents of the template
     */
    def this(template: String) =
        this(None, new ScalastiStringTemplate(template))

    /**
     * Set attribute named `attrName` to one or many different values.
     * Internally, a single value is stored as is, and multiple values are
     * coalesced into a `java.util.List` of type `T`. To pass a Scala list
     * (or sequence) in, use this syntax:
     *
     * {{{
     * template.setAttribute("name", List(1, 2, 3): _*)
     * }}}
     *
     * @tparam T        the type of the values to assign to the attribute
     * @param attrName  the name of the attribute
     * @param values    one or more values to associate with the attribute
     *
     * @return this object, for convenience
     */
    def setAttribute[T](attrName: String, values: T*): StringTemplate =
    {
        values.toList match
        {
            case value :: Nil =>
                val valueAny = value.asInstanceOf[Any]
                attributeMap += attrName -> valueAny
                template.setAttribute(attrName, valueAny)

            case value :: tail =>
                attributeMap += (attrName -> values)
                template.setAttribute(attrName, toJavaList(values))

            case _ =>
        }

        this
    }

    /**
     * Set attribute named `attrName` to many different values. Internally,
     * the values are coalesced into a `java.util.List` of type `T`.
     *
     * @tparam T        the type of the values to assign to the attribute
     * @param attrName  the name of the attribute
     * @param values    the values to associate with the attribute
     *
     * @return this object, for convenience
     */
    def setAttribute[T](attrName: String, values: Iterator[T]): StringTemplate =
    {
        attributeMap += (attrName -> values)
        template.setAttribute(attrName, toJavaList(values.toSeq))
        this
    }

    /**
     * Replace the current set of attributes with the attributes in the
     * specified map. Multivalued attributes are supported via Scala
     * sequences and iterators, as described in the class documentation.
     *
     * @param newAttrs  the map of new attributes
     *
     * @return this object, for convenience
     */
    def setAttributes(newAttrs: Map[String, Any]): StringTemplate =
    {
        attributeMap.clear()
        attributeMap ++= newAttrs
        template.setAttributes(mapToJavaMap(attributes))
        this
    }

    /**
     * Set an automatic aggregate from the specified arguments. An
     * automatic aggregate looks like an object from within a template, but
     * it isn't backed by a bean. Instead, you specify the aggregate with a
     * special syntax. For instance, the following code defines an
     * aggregate attribute called `name`, with two fields, `first` and
     * `last`. Those fields can be interpolated within a template via
     * `$item.first$` and `$item.last$`.
     *
     * {{{
     * val st = new StringTemplate( ... )
     * st.setAggregate("name.{first,last}", "Moe", "Howard")
     * }}}
     *
     * Setting the same aggregate multiple times results in a list of
     * aggregates:
     *
     * {{{
     * val st = new StringTemplate( ... )
     * st.setAggregate("name.{first,last}", "Moe", "Howard")
     * st.setAggregate("name.{first,last}", "Larry", "Fine")
     * st.setAggregate("name.{first,last}", "Curley", "Howard")
     * }}}
     *
     * Note, however, that this syntax does not support nested aggregates.
     * Use the map version of `setAggregate()` for that.
     *
     * See
     * http://www.antlr.org/wiki/display/ST/Expressions#Expressions-Automaticaggregatecreation
     * for more information.
     *
     * @param aggrSpec  the spec, as described above
     * @param values    one or more values. The values are treated as discrete;
     *                  that is, lists are not supported.
     *
     * @return this object, for convenience
     */
    def setAggregate(aggrSpec: String, values: Any*): StringTemplate =
    {
        def isOfType[T](v: Any)(implicit man: Manifest[T]): Boolean =
            man >:> Manifest.classType(v.asInstanceOf[AnyRef].getClass)

        def transform(v: Any) =
        {
            if (isOfType[Seq[Any]](v))
                toJavaList(v.asInstanceOf[Seq[Any]])

            else if (isOfType[Iterator[Any]](v))
                toJavaList(v.asInstanceOf[Iterator[Any]].toList)

            else
                v
        }

        val valuesAsObjects = values.map(transform(_).asInstanceOf[Object])
        template.setAggregate(aggrSpec, valuesAsObjects.toArray)
        this
    }

    /**
     * Create a "mapped aggregate". The supplied map's keys are used as the
     * fields of the aggregate. With a mapped aggregate, Scalasti actually
     * translates the map into a Java Bean, which it then uses to set the
     * attribute. Because Scalasti recursively converts all maps it finds
     * (as long as they are of type `Map[String, Any]`), a mapped attribute
     * can handle nested attribute references.
     *
     * The underlying StringTemplate library does _not_ support the notion
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
     * $thing.foo$                  # expands to "FOO"
     * $things.alien.firstName$     # expands to "John"
     * $things.alien.lastName$      # expands to "Smallberries"
     * }}}
     *
     * @param attrName  the attribute's name (i.e., the outermost name)
     * @param valueMap  the map of attribute fields
     *
     * @return this object, for convenience
     */
    def setAggregate(attrName: String,
                     valueMap: Map[String, Any]): StringTemplate =
    {
        import org.clapper.classutil.MapToBean

        if (! valueMap.isEmpty)
            setAttribute(attrName, MapToBean(valueMap))

        this
    }

    /**
     * Unset the value of the named attribute. Corresponds to the underlying
     * StringTemplate API's `removeAttribute()` call.
     *
     * @param attrName the name of the attribute to unset
     *
     * @return this object, for convenience
     */
    def unsetAttribute(attrName: String): StringTemplate =
    {
        attributeMap -= attrName
        template.removeAttribute(attrName)
        this
    }

    /**
     * Reset the template, clearing all its associated values.
     */
    def reset(): Unit =
    {
        attributeMap.clear
        template.reset()
    }

    /**
     * Set the `isRegion` flag.
     *
     * @param flag `true` to set the flag, `false` to clear it.
     */
    def isRegion(flag: Boolean): Unit = template.setIsRegion(flag)

    /**
     * Get a map containing all attribute names and values.
     *
     * @return a read-only map of attributes.
     */
    def attributes = Map.empty[String, Any] ++ attributeMap

    /**
     * Register an attribute renderer for a specific type. The
     * attribute renderer object must implement the `AttributeRenderer`
     * trait for the specific type.
     *
     * @tparam T         the type of attribute that the renderer can render
     * @param  renderer  the attribute renderer to use for values of type `T`
     *
     * @return this object, for convenience
     */
    def registerRenderer[T](renderer: AttributeRenderer[T])
                           (implicit mT: scala.reflect.Manifest[T]) =
    {
        template.registerRenderer(mT.erasure, renderer.stRenderer)
        this
    }

    /**
     * Returns a copy of the underlying (wrapped) StringTemplate API object.
     * Unlike `StringTemplate.getInstanceOf()`, this method copies the
     * current set of attributes and the enclosing instance reference (if any)
     * to the returned copy.
     *
     * @return a copy of the underlying StringTemplate object.
     */
    def nativeTemplate: ST_StringTemplate =
    {
        val copy = template.getInstanceOf
        val enclosingInstance = template.getEnclosingInstance

        if (enclosingInstance != null)
            copy.setEnclosingInstance(enclosingInstance)

        copy.setAttributes(mapToJavaMap(attributes))
        copy
    }

    /**
     * Get the current error listener, which is notified when errors occur.
     *
     * @return the error listener
     */
    def errorListener = template.getErrorListener

    /**
     * Set the current error listener, which is notified when errors occur.
     *
     * @param listener  the error listener
     */
    def errorListener_=(listener: StringTemplateErrorListener) =
        template.setErrorListener(listener)

    /**
     * Get the template's name, if any.
     *
     * @return the name, or `None`
     */
    def name = template.getName match
    {
        case null      => None
        case s: String => Some(s)
    }

    /**
     * Set or change the template's name.
     *
     * @param name  the new name. Must not be null.
     */
    def name_=(name: String) =
    {
        require(name != null)
        template.setName(name)
    }

    /**
     * Get the template's internally assigned ID.
     *
     * @return the ID
     */
    def ID = template.getTemplateID

    /**
     * Render the template with the current attributes.
     *
     * @return the rendered template.
     */
    override def toString = template.toString

    /* ---------------------------------------------------------------------- *\
                             Protected Methods
    \* ---------------------------------------------------------------------- */

    /**
     * Maps a Scala map of attributes into a Java map of attributes. The
     * Scala map is converted to a `java.util.HashMap`. The keys are
     * assumed to be strings. The values are mapped as follows:
     *
     * - A Scala `Seq` (which includes lists and array buffers) is mapped to
     *   a `java.util.List`, so it's treated as a multivalued attribute by the
     *   underlying StringTemplate library.
     * - A Scala iterator is also mapped to a `java.util.List`.
     * - Anything else is treated as a single-valued object.
     *
     * To enhance how these mappings are done, override this method.
     *
     * @param map  The Scala map to convert.
     *
     * @return the Java map
     */
    protected def mapToJavaMap(map: Map[String, Any]): JMap[String, Object] =
    {
        val result = new JHashMap[String, Object]

        // Adapted from
        // http://fupeg.blogspot.com/2009/10/scala-manifests-ftw.html

        def transform(k: String, v: Any) =
        {
            if (getType[Seq[Any]](map, k) != None)
                // Found a sequence. Use an ArrayList.
                toJavaList(v.asInstanceOf[Seq[Any]])

            else if (getType[Iterator[Any]](map, k) != None)
                // Found an iterator. Use an ArrayList.
                toJavaList(v.asInstanceOf[Iterator[Any]].toList)

            else
                v.asInstanceOf[Object]
        }

        map.foreach(kv => result.put(kv._1, transform(kv._1, kv._2)))
        result
    }

    /**
     * Given a Scala map, with string keys and any kind of value, retrieve
     * the value for a key (if any) only if it conforms to a specific type.
     * Otherwise, return None.
     *
     * @tparam T  the type to which the result must conform
     * @param map the map
     * @param key the key
     */
    protected def getType[T](map: Map[String, Any], key: String)
                            (implicit man: Manifest[T]): Option[T] =
    {
        map.getOrElse(key, null) match
        {
            case v: Any =>
                if (man >:> Manifest.classType(v.asInstanceOf[AnyRef].getClass))
                    Some(v.asInstanceOf[T])
                else
                    None

            case null =>
                None
        }
    }

    /* ---------------------------------------------------------------------- *\
                              Private Methods
    \* ---------------------------------------------------------------------- */

    /**
     * Convert a Scala sequence to a Java list.
     *
     * @param seq  the sequence
     *
     * @param list the list
     */
    private def toJavaList[T](seq: Seq[T]): JList[T] =
    {
        val list = new JArrayList[T]
        seq.foreach(list.add(_))
        list
    }
}

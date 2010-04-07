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

import org.antlr.stringtemplate.{StringTemplateGroup => ST_StringTemplateGroup,
                                 StringTemplate => ST_StringTemplate}
import scalaj.collection.Imports._

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
 * - A Scala `Seq` (which includes lists and array buffers) is mapped to
 *   a `java.util.List`, so it's treated as a multivalued attribute by the
 *   underlying StringTemplate library.
 * - A Scala iterator is also mapped to a `java.util.List`.
 * - Anything else is treated as a single-valued object.
 *
 * @param group     the StringTemplateGroup in which the template resides
 * @param template  the real, underlying String Template
 */
class StringTemplate(val group: Option[StringTemplateGroup],
                     private val template: ST_StringTemplate)
{
    private val attributeMap = MutableMap.empty[String, AnyRef]

    /**
     * Alternate constructor that takes an unnamed, ungrouped template, as
     * contained in a string.
     *
     * @param template  the contents of the template
     */
    def this(template: String) = this(None, new ST_StringTemplate(template))

    /**
     * Set attribute named `attrName` to one or many different values.
     * Internally, a single value is stored as is, and multiple values are
     * coalesced into a `java.util.List` of type `T`. To pass a Scala list
     * (or sequence) in, use this syntax:
     * {{{
     * template.setAttribute("name", List(1, 2, 3): _*)
     * }}}
     *
     * @param attrName  the name of the attribute
     * @param values    one or more values to associate with the attribute
     */
    def setAttribute[T](attrName: String, values: T*): Unit =
    {
        values.toList match
        {
            case value :: Nil =>
                val valueAny = value.asInstanceOf[AnyRef]
                attributeMap += attrName -> valueAny
                template.setAttribute(attrName, valueAny)

            case value :: tail =>
                attributeMap += (attrName -> values)
                template.setAttribute(attrName, values.asJava)

            case _ =>
        }
    }

    /**
     * Set attribute named `attrName` to many different values. Internally,
     * the values are coalesced into a `java.util.List` of type `T`.
     *
     * @param attrName  the name of the attribute
     * @param values    the values to associate with the attribute
     */
    def setAttribute[T](attrName: String, values: Iterator[T]): Unit =
    {
        attributeMap += (attrName -> values)
        template.setAttribute(attrName, values.asJava)
    }

    /**
     * Replace the current set of attributes with the attributes in the
     * specified map. Multivalued attributes are supported via Scala
     * sequences and iterators, as described in the class documentation.
     *
     * @param newAttrs  the new attributes
     */
    def setAttributes(newAttrs: Map[String, AnyRef]) =
    {
        attributeMap.clear()
        attributeMap ++= newAttrs
        template.setAttributes(mapToJavaMap(attributes))
    }

    /**
     * Unset the value of the named attribute. Corresponds to the underlying
     * StringTemplate API's `removeAttribute()` call.
     *
     * @param attrName the name of the attribute to unset
     */
    def unsetAttribute(attrName: String): Unit =
    {
        attributeMap -= attrName
        template.removeAttribute(attrName)
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
     */
    def isRegion(flag: Boolean): Unit = template.setIsRegion(flag)

    /**
     * Get a map containing all attribute names and values.
     *
     * @return a read-only map of attributes.
     */
    def attributes = Map.empty[String, AnyRef] ++ attributeMap

    /**
     * Register an attribute renderer for a specific type. The
     * attribute renderer object must implement the `AttributeRenderer`
     * trait for the specific type.
     *
     * @param attrRenderer the attribute renderer to use for values of type `T`
     */
    def registerRenderer[T](attrRenderer: AttributeRenderer[T])
                           (implicit mT: scala.reflect.Manifest[T]) =

    {
        template.registerRenderer(mT.erasure, attrRenderer.stRenderer)
    }

    /**
     * Returns a copy of the underlying (wrapped) StringTemplate API object.
     * Unlike `StringTemplate.getInstanceOf()`, this method copies the
     * current set of attributes and the enclosing instance reference (if any)
     * to the returned copy.
     *
     * @return a copy of the underlying StringTemplate object.
     */
    def nativeTemplate =
    {
        val copy = template.getInstanceOf
        val enclosingInstance = template.getEnclosingInstance

        if (enclosingInstance != null)
            copy.setEnclosingInstance(enclosingInstance)

        copy.setAttributes(mapToJavaMap(attributes))
        copy
    }

    /**
     * Render the template with the current attributes.
     *
     * @return the rendered template.
     */
    override def toString = template.toString

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
    protected def mapToJavaMap(map: Map[String, AnyRef]): JMap[String, Object] =
    {
        val result = new JHashMap[String, AnyRef]

        // Adapted from
        // http://fupeg.blogspot.com/2009/10/scala-manifests-ftw.html

        def toJList(seq: Seq[Any]): JList[Any] =
        {
            val list = new JArrayList[Any]
            seq.foreach(list.add(_))
            list
        }

        for ((k, v) <- map)
        {
            if (getType[Seq[Any]](map, k) != None)
                // Found a sequence. Use an ArrayList.
                result.put(k, toJList(v.asInstanceOf[Seq[Any]]))

            else if (getType[Iterator[AnyRef]](map, k) != None)
                // Found an iterator. Use an ArrayList.
                result.put(k, toJList(v.asInstanceOf[Iterator[Any]].toList))

            else
                result.put(k, v)
        }

        result
    }

    /**
     * Given a Scala map, with string keys and any kind of value, retrieve
     * the value for a key (if any) only if it conforms to a specific type.
     * Otherwise, return None.
     *
     * @param map the map
     * @param key the key
     */
    protected def getType[T](map: Map[String, Any], key: String)
                            (implicit man: Manifest[T]): Option[T] =
    {
        map.getOrElse(key, null) match
        {
            case value: AnyRef =>
                if (man >:> Manifest.classType(value.getClass))
                    Some(value.asInstanceOf[T])
                else
                    None

            case null =>
                None
        }
    }
}

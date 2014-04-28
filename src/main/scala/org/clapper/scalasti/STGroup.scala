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

import org.stringtemplate.v4.{STGroup => _STGroup}
import org.antlr.runtime.Token

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try
import scala.collection.JavaConverters._
import java.net.URL

/** A Scala wrapper for the String Template library's `STGroup` class. This
  * class provides access to most of the methods on the underlying class,
  * with Scala semantics, where appropriate. This class cannot be instantiated
  * directly; use the `apply()` methods on the companion object, or create one
  * of the subclasses.
  *
  * This class does not expose all of the underlying functions of the actual
  * StringTemplate API. If you need access to the full Java StringTemplate API,
  * you can retrieve the underlying `STGroup` by calling the `nativeGroup`
  * method.
  */
class STGroup(private[scalasti] val native: _STGroup) {

  /** Get the underlying Java StringTemplate `STGroup` object.
    *
    * @return the underlying `STGroup`
    */
  def nativeSTGroup = native

  /** Get the template names defined by the group.
    *
    * @return a set of the template names supplied by this group
    */
  def templateNames: Set[String] = native.getTemplateNames().asScala.toSet

  /** Determine whether a named template is defined in this group. The names
    * must be fully-qualified template paths (e.g., "/g1/name")
    *
    * @param name  the template name
    *
    * @return `true` if defined, `false` if not
    */
  def isDefined(name: String): Boolean = native.isDefined(name)

  /** Get the root directory, if this is the group directory, or the group
    * file, if this is a group file.
    *
    * @return the root
    */
  def rootDirURL: URL = native.getRootDirURL

  /** Get the group's name.
    *
    * @return the group name
    */
  def name: String = native.getName()

  /** Get the group file name.
    *
    * @return the file name
    */
  def fileName: String = native.getFileName

  /** Force a load. Templates are normally loaded on demand; this method
    * attempts to load them up front. '''Note''': Even though this method
    * attempts to detect failure, the underlying StringTemplate method seems
    * to trap load errors and display them on standard output, ''without''
    * passing them up the stack. Testing for errors might not return what
    * you expect.
    *
    * @return `Success(Unit)` on success. `Failure(exception)` on load failure.
    */
  def load(): Try[Unit] = Try { native.load() }

  /** Force an unload.
    */
  def unload() = native.unload()

  /** Get an instance of a template defined in group.
    *
    * @param templateName  the name of the template
    *
    * @return `Success(ST)` if the template was found and loaded.
    *         `Failure(exception)` if the template could not be demand-loaded.
    */
  def instanceOf(templateName: String): Try[ST] = {
    Try {
      Option(native.getInstanceOf(templateName)).map {
        new ST(_)
      }.
      getOrElse {
        throw new Exception(s"Unable to get an instance of $templateName")
      }
    }
  }

  /** Register a renderer for a particular type. When the underlying
    * StringTemplate API attempts to render a template, it'll use this
    * renderer when it encounters values of this type.
    *
    * '''NOTE''': By default, Scalasti automatically wraps Scala objects
    * in dynamically generated Java Beans when they're added to templates,
    * because the StringTemplate API uses Java Bean semantics to access
    * object fields. For this reason, you cannot use an `AttributeRenderer`
    * unless you add the values it is to render as raw objects. See the
    * `ST.add()` method for details.
    *
    * @param r
    * @tparam T
    *
    * @see [[ST.add]]
    */
  def registerRenderer[T: ru.TypeTag](r: AttributeRenderer[T]): Unit = {
    val tpe = ru.typeTag[T].tpe
    val cls = runtimeMirror(r.getClass.getClassLoader).runtimeClass(tpe)

    native.registerRenderer(cls, r.stRenderer)
  }
}

object STGroup {
  def apply(delimiterStartChar: Char = Constants.DefaultStartChar,
            delimiterStopChar:  Char = Constants.DefaultStopChar): STGroup = {
    new STGroup(new _STGroup(delimiterStartChar, delimiterStopChar))
  }
}

/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010-2014 Brian M. Clapper
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

import java.io.File

import org.stringtemplate.v4.{AttributeRenderer => _AttributeRenderer,
                              STGroup => _STGroup}

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try
import scala.collection.JavaConverters._
import java.net.URL

import org.stringtemplate.v4.misc.ErrorManager

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
case class STGroup(
  delimiterStartChar: Char = Constants.DefaultStartChar,
  delimiterStopChar:  Char = Constants.DefaultStopChar,
  private[scalasti] val nativeOpt: Option[_STGroup] = None,
  private[scalasti] val renderers: Map[Class[_], _AttributeRenderer] =
    Map.empty[Class[_], _AttributeRenderer]
) {

  private val native: _STGroup = nativeOpt.map { n =>
    applyRenderers(n)
    n
  }
  .getOrElse {
    cloneUnderlying
  }

  /** Get the underlying Java StringTemplate `STGroup` object.
    *
    * @return the underlying `STGroup`
    */
  def nativeGroup: _STGroup = native

  /** Get the template names defined by the group. Note that calling
    * this method on an object for which `prepare()` has not been called causes
    * instantiation of a new, underlying, throwaway `STGroup` object each time,
    * to preserve immutability.
    *
    * @return a set of the template names supplied by this group
    */
  def templateNames: Set[String] = native.getTemplateNames.asScala.toSet

  /** Determine whether a named template is defined in this group. The names
    * must be fully-qualified template paths (e.g., "/g1/name"). Note that
    * calling this method on an object for which `prepare()` has not been called
    * causes instantiation of a new, underlying, throwaway `STGroup` object
    * each time, to preserve immutability.
    *
    * @param name  the template name
    *
    * @return `true` if defined, `false` if not
    */
  def isDefined(name: String): Boolean = native.isDefined(name)

  /** Get the root directory, if this is the group directory, or the group
    * file, if this is a group file. Note that calling this method on an object
    * for which `prepare()` has not been called causes instantiation of a new,
    * underlying, throwaway `STGroup` object each time, to preserve
    * immutability.
    *
    * @return the root
    */
  def rootDirURL: URL = native.getRootDirURL

  /** Get the group's name.
    *
    * @return the group name
    */
  def name: String = native.getName

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
    * @return `Success(newSTGroup)` on success. `Failure(exception)` on load
    *         failure.
    */
  def load(): Try[STGroup] = Try {
    val underlying = cloneUnderlying
    underlying.load()
    this.copy(nativeOpt = Some(underlying))
  }

  /** Force an unload. Returns a new `STGroup` with an unloaded native
    * StringTemplate `STGroup`.
    *
    * @return the new object
    */
  def unload(): STGroup = {
    val underlying = cloneUnderlying
    underlying.unload()
    this.copy(nativeOpt = Some(underlying))
  }

  /** Get an instance of a template defined in group.  Note that calling this
    * method on an object for which `prepare()` has not been called causes
    * instantiation of a new, underlying, throwaway `STGroup` object each time,
    * to preserve immutability.
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
    * @param r   the renderer
    * @tparam T  the type
    *
    * @return a new `STGroup` object with the new renderer
    *
    * @see [[ST.add]]
    */
  def registerRenderer[T: ru.TypeTag](r: AttributeRenderer[T]): STGroup = {
    val tpe = ru.typeTag[T].tpe
    val cls = runtimeMirror(r.getClass.getClassLoader).runtimeClass(tpe)

    this.copy(renderers = this.renderers + (cls -> r.stRenderer))
  }

  // --------------------------------------------------------------------------
  // Protected methods
  // --------------------------------------------------------------------------

  /** Create a new underlying StringTemplate object, applying whatever
    * constructor parameters were used with the current object. Does not
    * apply the renderers.
    *
    * Subclasses should override this method.
    *
    * @return the new underlying object
    */
  protected[this] def newUnderlying: _STGroup = {
    new _STGroup(delimiterStartChar, delimiterStopChar)
  }

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  /** Create a new clone of the underlying StringTemplate `STGroupFile` object,
    * applying all renderers.
    *
    * @return the clone
    */
  private def cloneUnderlying: _STGroup = {
    val underlying = newUnderlying
    makeErrorsExceptions(underlying)
    applyRenderers(underlying)
    underlying
  }

  /** Ensure that any errors that occur during template processing result
    * in a thrown exception that can be captured in a `Try`.
    *
    * @param underlying the native `STGroup` object whose error handling is to
    *                   be changed
    */
  private def makeErrorsExceptions(underlying: _STGroup): Unit = {
    underlying.errMgr = new ErrorManager(new ThrowExceptionErrorListener)
  }

  /** Apply all renderers to an underlying StringTemplate `STGroup` (or
    * derived class).
    *
    * @param underlying the underlying `STGroup`
    */
  private def applyRenderers(underlying: _STGroup): Unit = {
    for ( (cls, renderer) <- this.renderers ) {
      underlying.registerRenderer(cls, renderer)
    }
  }
}


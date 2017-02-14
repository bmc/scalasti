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

import org.stringtemplate.v4.{AttributeRenderer => _AttributeRenderer}
import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.runtime.{universe => ru}
import java.util.{Date, Locale}
import java.lang.{Object => JObject}

/**
  * A more Scala-like ST attribute renderer. Objects that implement this
  * trait can be registered as attribute attrRenderers with an STGroupString.
  *
  * @tparam T  the type (class) for which the renderer can render values.
  */
trait AttributeRenderer[T] {
  private val self = this

  // The actual Java renderer used by ST.
  private[scalasti] val stRenderer = new _AttributeRenderer {
    def toString(o: JObject, format: String, locale: Locale): String = {
      self.toString(o.asInstanceOf[T], format, locale)
    }
  }

  /** Converts an object of type `T` to a string, for inclusion in a template.
    *
    * @param o            the object
    * @param formatString format string to use
    * @param locale       the locale
    */
  def toString(o: T, formatString: String, locale: Locale): String
}

/** A Scalasti version of String Template's `NumberRenderer`.
  */
class NumberRenderer extends AttributeRenderer[Number] {
  private val nr = new org.stringtemplate.v4.NumberRenderer()

  /** Formats a number to a string, for inclusion in a template.
    *
    * @param n       the number
    * @param format  format string to use
    * @param locale  the locale
    */
  def toString(n: Number, format: String, locale: Locale): String = {
    nr.toString(n, format, locale)
  }
}

/** A Scalasti version of String Template's `DateRenderer`.
  */
class DateRenderer extends AttributeRenderer[Date] {
  private val dr = new org.stringtemplate.v4.DateRenderer()

  /** Formats a date to a string, for inclusion in a template.
    *
    * @param date    the date
    * @param format  format string to use
    * @param locale  the locale
    */
  def toString(date: Date, format: String, locale: Locale): String = {
    dr.toString(date, format, locale)
  }
}

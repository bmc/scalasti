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

import org.antlr.stringtemplate.{StringTemplateGroup => ST_StringTemplateGroup}

import java.io.File

import scala.io.Source

import grizzled.io.SourceReader

/**
 * A Scala wrapper for the String Template library's `StringTemplateGroup`
 * class.
 *
 * @param group the actual, underlying String Template library group object.
 */
class StringTemplateGroup(private val group: ST_StringTemplateGroup)
{
    /**
     * Alternate constructor that creates a template group manager for
     * templates that are at, or below, the specified directory.
     *
     * @param groupName  the group's name
     * @param directory  the directory containing the templates
     */
    def this(groupName: String, directory: File) =
        this(new ST_StringTemplateGroup(groupName, directory.getPath))
        
    /**
     * Alternate constructor that creates a template group manager for
     * templates that are to be loaded as resources via the class loader.
     *
     * @param groupName  the group's name
     */
    def this(groupName: String) =
        this(new ST_StringTemplateGroup(groupName))

    /**
     * Alternate constructor that creates a template group from the group
     * defined by a readable source. Useful for reading template group files.
     *
     * @param source        the source from which to read
     * @param errorListener an optional error listener to receive errors
     */
    def this(source: Source) = 
        this(new ST_StringTemplateGroup(SourceReader(source)))

    /**
     * Alternate constructor that creates a template group from the group
     * defined by a readable source. Useful for reading template group files.
     *
     * @param source        the source from which to read
     * @param errorListener an error listener to receive errors
     */
    def this(source: Source, errorListener: StringTemplateErrorListener) =
        this(new ST_StringTemplateGroup(SourceReader(source), errorListener))

    /**
     * Returns a copy of the underlying (wrapped) StringTemplate API object.
     *
     * @return a copy of the underlying StringTemplateGroup object.
     */
    def nativeTemplateGroup =
    {
        // Doesn't actually return a copy in this case.

        group
    }

    /**
     * Determine whether this group contains a template with a given name.
     *
     * @param name  the template name to check
     *
     * @return `true` if a template exists by that name, `false` if not
     */
    def isDefined(name: String) = group.isDefined(name)

    /**
     * Get the refresh interval, which defines how often templates are
     * refreshed from disk. An interval of 0 means there's no caching, and
     * templates are loaded every time they are retrieved; any other value
     * represents how long, in milliseconds, to cache templates in memory
     * before checking disk again to see if they've changed.
     *
     * @return the refresh interval
     */
    def refreshInterval = group.getRefreshInterval

    /**
     * Set the refresh interval, which defines how often templates are
     * refreshed from disk. An interval of 0 means there's no caching, and
     * templates are loaded every time they are retrieved; any other value
     * represents how long, in milliseconds, to cache templates in memory
     * before checking disk again to see if they've changed.
     *
     * @param interval  the new refresh interval
     */
    def refreshInterval_=(interval: Int) = group.setRefreshInterval(interval)

    /**
     * Create an empty template within this group. This method corresponds
     * to the underlying API's `createStringTemplate()` method.
     *
     * @return the empty template
     */
    def newEmptyTemplate = group.createStringTemplate()

    /**
     * Create a new template and associate it with the specified name
     * within the group. If the group already contains a template with the
     * same name, this new template replaces the existing template.
     *
     * @param name     the template name
     * @param contents the template's contents (i.e., the template string)
     *
     * @return the template object
     */
    def defineTemplate(name: String, contents: String): StringTemplate =
        new StringTemplate(Some(this), group.defineTemplate(name, contents))

    /**
     * Equivalent to the String Template library's `getInstanceOf()` method,
     * this method returns the template with the specified name, returning
     * the template if found, or `None` if not.
     *
     * @param templateName the template name
     *
     * @return the template. Throws an exception if the template isn't found.
     */
    def template(templateName: String): StringTemplate =
        new StringTemplate(Some(this), group.getInstanceOf(templateName))

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
        group.registerRenderer(mT.erasure, attrRenderer.stRenderer)
    }

    /**
     * Get the current error listener, which is notified when errors occur.
     *
     * @return the error listener
     */
    def errorListener = group.getErrorListener

    /**
     * Set the current error listener, which is notified when errors occur.
     *
     * @param listener  the error listener
     */
    def errorListener_=(listener: StringTemplateErrorListener) =
        group.setErrorListener(listener)

}

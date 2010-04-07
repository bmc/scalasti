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
class StringTemplateGroup(val group: ST_StringTemplateGroup)
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
     * Equivalent to the String Template library's `getInstanceOf()` method,
     * this method returns the template with the specified name, returning
     * the template if found, or `None` if not.
     *
     * @param templateName the template name
     */
    def template(templateName: String): Option[StringTemplate] =
    {
        try
        {
            Some(new StringTemplate(Some(this),
                                    group.getInstanceOf(templateName)))
        }

        catch
        {
            case e: IllegalArgumentException => None
        }
    }
}

/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

  * Neither the names "clapper.org", "Scalasti", nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

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
\*---------------------------------------------------------------------------*/

import org.scalatest.FunSuite
import org.clapper.scalasti._

/**
 * Tests the grizzled.io functions.
 */
class StringTemplateTest extends ScalastiFunSuite
{
    test("render template #1")
    {
        val template = """This is a $test$ template: $many; separator=", "$"""

        val data = List(
            (Map("test" -> "test",
                 "many" -> List("a", "b", "c")),
             """This is a test template: a, b, c"""),

            (Map("test" -> "foo",
                 "many" -> List("moe", "larry", "curley")),
             """This is a foo template: moe, larry, curley""")
        )

        for((attributes, expected) <- data)
            expect(expected, "render template on: " + attributes)
            {
                val st = new StringTemplate(template)
                st.setAttributes(attributes)
                st.toString
            }
    }

    test("render template #2")
    {
        val template = """This is a $test$ template: $many; separator=", "$"""

        val data = List(
            (Map("test" -> true,
                 "many" -> List("a", "b", "c")),
             """This is a true template: a, b, c""")
        )

        for((attributes, expected) <- data)
            expect(expected, "render template on: " + attributes)
            {
                val st = new StringTemplate(template)
                st.setAttributes(attributes)
                st.toString
            }
    }

    test("ValueRenderer")
    {
        val template = """This is a $test$ template"""

        class Value(val s: String)
        class ValueRenderer extends AttributeRenderer[Value]
        {
            def toString(v: Value) = "<" + v.s + ">"
        }

        expect("This is a <foo> template", "ValueRenderer")
        {
            val st = new StringTemplate(template)
            st.setAttribute("test", new Value("foo"))
            st.registerRenderer(new ValueRenderer)
            st.toString
        }
    }

    test("Automatic aggregates")
    {
        val template = """$if (page.title)$$page.title$$else$No title$endif$
                       |$page.categories; separator=", "$""".stripMargin

        val data = List(
            ("No title\nfoo, bar",
             "page.{categories}",
             List(List("foo", "bar"))),

            ("Foo\nmoe, larry, curley", 
             "page.{title, categories}",
             List("Foo", List("moe", "larry", "curley")))
        )

        for ((expected, aggrSpec, args) <- data)
        {
            expect(expected, "aggregate")
            {
                val st = new StringTemplate(template)
                st.setAggregate(aggrSpec, args: _*)
                st.toString
            }
        }
    }
}

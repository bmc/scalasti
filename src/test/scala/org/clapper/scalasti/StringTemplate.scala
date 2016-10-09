/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010-2014 Brian M. Clapper. All rights reserved.

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
  ---------------------------------------------------------------------------
*/

import java.io.{File, FileWriter}
import java.util.Locale
import org.scalatest.FunSuite
import org.clapper.scalasti._
import grizzled.io.util._

/**
  * Tests the grizzled.io functions.
  */
class StringTemplateTest extends FunSuite {
  // Type tags aren't available on nested classes (i.e., classes inside a
  // function).
  case class Value(s: String)

  test("render template #1") {
    val template = """This is a <test> template: <many; separator=", ">"""

    val data = List(
      (Map("test" -> "test",
           "many" -> List("a", "b", "c")),
       """This is a test template: a, b, c"""),

      (Map("test" -> "foo",
           "many" -> List("moe", "larry", "curley")),
       """This is a foo template: moe, larry, curley""")
    )

    for((attributes, expected) <- data)
      assertResult(expected, "render template on: " + attributes) {
        val st = ST(template)
        st.setAttributes(attributes)
        st.render()
      }
  }

  test("render template #2, with '$' delimiters") {
    val template = """This is a $test$ template: $many; separator=", "$"""

    val data = List(
      (Map("test" -> true,
           "many" -> List("a", "b", "c")),
       """This is a true template: a, b, c""")
    )

    for((attributes, expected) <- data)
      assertResult(expected, "render template on: " + attributes) {
        val st = ST(template, '$', '$')
        st.setAttributes(attributes)
        st.render()
      }
  }

  test("Template group from file") {
    val groupString =
      """
        |delimiters "$", "$"
        |t1(firstName, lastName) ::= <<Hello, $firstName$ $lastName$>>
        |t2(firstName, lastName) ::= <<$lastName$, $firstName$>>
      """.stripMargin

    val temp = File.createTempFile("scalasti", ".stg")
    temp.deleteOnExit()
    withCloseable(new FileWriter(temp)) { out =>
      out.write(groupString)
    }

    val group = STGroupFile(temp.getAbsolutePath)
    val stTry = group.instanceOf("t1")
    assert(stTry.map(t => true).getOrElse(false))

    val st = stTry.get
    st.add("firstName", "Curley")
    st.add("lastName", "Howard")
    assert(st.render() === "Hello, Curley Howard")

    val stTry2 = group.instanceOf("t2")
    assert(stTry2.map(t => true).getOrElse(false))

    val st2 = stTry2.get
    st2.add("firstName", "Larry")
    st2.add("lastName", "Fine")
    assert(st2.render() === "Fine, Larry")

    temp.delete()
  }

  test("ValueRenderer") {
    val groupString =
      """
        |delimiters "<", ">"
        |test(x) ::= <<This is a <x> template>>
      """.stripMargin

    object ValueRenderer extends AttributeRenderer[Value] {
      def toString(v: Value, formatString: String, locale: Locale) = {
        "<" + v.s + ">"
      }
    }

    val g = STGroupString(groupString, delimiterStartChar = '<', delimiterStopChar = '<')
    g.registerRenderer(ValueRenderer)
    val stTry = g.instanceOf("test")
    assert(stTry.map(t => true).getOrElse(false))

    assertResult("This is a <foo> template", "ValueRenderer") {
      val st = stTry.get
      st.add("x", Value("foo"), raw = true)
      st.render()
    }
  }

  test("Automatic aggregates") {
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

    for ((expected, aggrSpec, args) <- data) {
      assertResult(expected, "aggregate") {
        ST(template, '$', '$').addAggregate(aggrSpec, args: _*).render()
      }
    }
  }

  test("Mapped aggregates") {
    val template = "<thing.outer.inner> <foo.bar> <foo.baz> " +
                   "<thing.outer.x> <thing.okay>"

    val thingMap = Map("okay"  -> "OKAY",
                       "outer" -> Map("inner" -> "an inner string",
                                      "x"     -> "something else"))
    val fooMap = Map("bar" -> "BARSKI",
                     "baz" -> 42)

    val expected = "an inner string BARSKI 42 something else OKAY"
    assertResult(expected, "mapped attribute") {
      ST(template).addMappedAggregate("thing", thingMap)
                  .addMappedAggregate("foo", fooMap)
                  .render()
    }
  }

  test("Multivalue attribute") {
    case class User(val firstName: String, val lastName: String) {
      override def toString = firstName + " " + lastName
    }

    val u1 = User("Elvis", "Presley")
    val u2 = User("Frank", "Sinatra")
    val users = u1 :: u2 :: Nil

    val t1 = "Hi, <user.firstName> <user.lastName>."
    assertResult("Hi, Elvis Presley.", "template expansion of u1") {
      ST(t1).add("user", u1).render()
    }

    val t2 = "<users; separator=\", \">"
    assertResult("Elvis Presley, Frank Sinatra", "multivalue") {
      ST(t2).add("users", users).render()
    }
  }

  test("Numeric typed attribute retrieval") {
    val st = ST("Point = (<x>, <y>)")

    st.add("x", 10)
    st.add("y", 20)

    assert(Some(10) === st.attribute[Int]("x"))
    assert(Some(20) === st.attribute[Int]("y"))
    assert(None === st.attribute[Double]("x"))
    assert(None === st.attribute[Double]("y"))
    assert("Point = (10, 20)" === st.render())
  }

  test("String typed attribute retrieval") {
    val st = ST("<s>")
    st.add("s", "foo")
    assert(st.render() === "foo")
    assert(Some("foo") === st.attribute[String]("s"))
    assert(None === st.attribute[Int]("s"))
  }

  test("Optional String typed attribute retrieval") {
    val st = ST("<s>")
    st.add("s", Some("foo"))
    assert(st.render() === "foo")
    assert(Some("foo") === st.attribute[String]("s"))
  }

  test("None typed attribute retrieval") {
    val st = ST("<s>")
    st.add("s", None)
    assert(st.render() === "")
    assert(None === st.attribute[AnyRef]("s"))
    assert(None === st.attribute[String]("s"))
  }

  test("Custom typed attribute retrieval") {
    val groupString =
      """
        |delimiters "$", "$"
        |template(x) ::= <<This is a $x$ template>>
      """.stripMargin

    object ValueRenderer extends AttributeRenderer[Value] {
      def toString(v: Value, formatString: String, locale: Locale) = {
        v.s
      }
    }

    val group = STGroupString(groupString)
    group.registerRenderer(ValueRenderer)
    val stTry = group.instanceOf("template")

    assert(stTry.map(t => true).getOrElse(false))
    val st = stTry.get
    st.add("x", Value("foo"), raw=true)
    assert(st.render() === "This is a foo template")

    assert(Some(Value("foo")) === st.attribute[Value]("x"))
  }
}

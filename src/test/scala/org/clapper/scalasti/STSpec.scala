package org.clapper.scalasti

import java.io.{File, FileWriter}
import java.util.Locale

import grizzled.util._
import grizzled.util.CanReleaseResource.Implicits.CanReleaseAutoCloseable

/**
  * Tests the grizzled.io functions.
  */
class STSpec extends BaseSpec {
  // Type tags aren't available on nested classes (i.e., classes inside a
  // function).
  case class Value(s: String)

  "render" should "render a simple template with simple substitutions" in {
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

  it should "render a template with '$' delimiters" in {
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

  it should "handle a template group from a file" in {
    val groupString =
      """
        |delimiters "$", "$"
        |t1(firstName, lastName) ::= <<Hello, $firstName$ $lastName$>>
        |t2(firstName, lastName) ::= <<$lastName$, $firstName$>>
      """.stripMargin

    val temp = File.createTempFile("scalasti", ".stg")
    temp.deleteOnExit()
    withResource(new FileWriter(temp)) { out =>
      out.write(groupString)
    }

    val group = STGroupFile(temp.getAbsolutePath)
    val stTry = group.instanceOf("t1")
    stTry.isSuccess shouldBe true

    val st = stTry.get
    st.add("firstName", "Curley")
    st.add("lastName", "Howard")
    st.render() shouldBe "Hello, Curley Howard"

    val stTry2 = group.instanceOf("t2")
    assert(stTry2.map(t => true).getOrElse(false))

    val st2 = stTry2.get
    st2.add("firstName", "Larry")
    st2.add("lastName", "Fine")
    st2.render() shouldBe "Fine, Larry"

    temp.delete()
  }

  it should "allow a custom ValueRenderer" in {
    val groupString =
      """
        |delimiters "<", ">"
        |test(x) ::= <<This is a <x> template>>
      """.stripMargin

    object ValueRenderer extends AttributeRenderer[Value] {
      def toString(v: Value, formatString: String, locale: Locale): String = {
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

  it should "handle automatic aggregates" in {
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

  it should "handle mapped aggregates" in {
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

  it should "handle multivalue attributes" in {
    case class User(firstName: String, lastName: String) {
      override def toString: String = firstName + " " + lastName
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

  it should "handle numeric typed attribute retrieval" in {
    val st = ST("Point = (<x>, <y>)")

    st.add("x", 10)
    st.add("y", 20)

    st.attribute[Int]("x") shouldBe Some(10)
    st.attribute[Int]("y") shouldBe Some(20)
    st.attribute[Double]("x") shouldBe None
    st.attribute[Double]("y") shouldBe None
    st.render() shouldBe "Point = (10, 20)"
  }

  it should "handle string typed attribute retrieval" in {
    val st = ST("<s>")
    st.add("s", "foo")
    st.render() shouldBe "foo"
    st.attribute[String]("s") shouldBe Some("foo")
    st.attribute[Int]("s") shouldBe None
  }

  it should "handle optional String typed attribute retrieval" in {
    val st = ST("<s>")
    st.add("s", Some("foo"))
    st.render() shouldBe "foo"
    st.attribute[String]("s") shouldBe Some("foo")
  }

  it should "handle None-typed attribute retrieval" in {
    val st = ST("<s>")
    st.add("s", None)
    st.render() shouldBe ""
    st.attribute[AnyRef]("s") shouldBe None
    st.attribute[String]("s") shouldBe None
  }

  it should "handle custom typed attribute retrieval" in {
    val groupString =
      """
        |delimiters "$", "$"
        |template(x) ::= <<This is a $x$ template>>
      """.stripMargin

    object ValueRenderer extends AttributeRenderer[Value] {
      def toString(v: Value, formatString: String, locale: Locale): String = {
        v.s
      }
    }

    val group = STGroupString(groupString)
    group.registerRenderer(ValueRenderer)
    val stTry = group.instanceOf("template")
    stTry.isSuccess shouldBe true

    val st = stTry.get
    st.add("x", Value("foo"), raw=true)
    st.render() shouldBe "This is a foo template"
    st.attribute[Value]("x") shouldBe Some(Value("foo"))
  }

  it should "properly substitute from a Some and a None" in {
    val st = ST("x=<x>, y=<y>")

    def add(label: String, o: Option[Int]) = st.add(label, o)

    add("x", Some(10))
    add("y", None)

    st.render() shouldBe "x=10, y="
  }
}

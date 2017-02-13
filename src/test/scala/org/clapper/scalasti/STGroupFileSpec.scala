package org.clapper.scalasti

import java.net.URL
import java.util.Locale


// Can't register renderers for primitive types.
class FloatWrapper(val f: Float)

/** Tests for STGroup class
  */
class STGroupFileSpec extends BaseSpec {

  val TemplateGroup1 = TemplateGroupData(
    path = "foo.stg",
    groupString = s"""|delimiters "%", "%"
                      |import "parens.st"
                      |import "foo.st"
                      |""".stripMargin,
    templates = Seq(
      TemplateData(
        path = "foo.st",
        templateString = """foo(names, values) ::= <<
                           |%names, values:{ n,v | %parens([n, v])%}; separator=" "%
                           |>>""".stripMargin
      ),
      TemplateData(path = "parens.st",
        templateString  = """parens(args) ::= <<
                            |(%args; separator=","%)
                            |>>""".stripMargin
      )
    )
  )

  val TemplateGroup2 = TemplateGroupData(
    path = "bar.stg",
    groupString = """|delimiters "$", "$"
                     |import "bar.st"
                  """.stripMargin,
    templates = Seq(
      TemplateData(
        path = "bar.st",
        templateString = """bar(args) ::= <<
                           |$args;separator="\u2014"$
                           |>>""".stripMargin
      )
    )
  )

  val TemplateGroup3 = TemplateGroupData(
    path = "baz.stg",
    groupString = """|delimiters "$", "$"
                     |import "subdir"
                  """.stripMargin,
    templates = Seq(
      TemplateData(
        path = "subdir/quux.st",
        templateString = """quux(args) ::= <<
                           |$args;separator=" "$
                           |>>""".stripMargin
      ),
      TemplateData(
        path = "subdir/blah.st",
        templateString = """blah(args) ::= <<
                           |$args;separator="-"$
                           |>>""".stripMargin
      )
    )
  )

  "load()" should "fail on a nonexistent file" in {
    val grp = STGroupFile(new URL("file:///tmp/foo.stg"))
    grp.load() shouldBe 'failure
  }

  it should "work when pointed at a legit file" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      stGroup.load() shouldBe 'success
    }
  }

  it should "be immutable" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      val t = stGroup.load()
      t shouldBe 'success
      val stGroup2 = t.get
      stGroup2 should not be stGroup
      stGroup.nativeGroup should not be stGroup2.nativeGroup
    }
  }

  "unload()" should "be immutable" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      val stGroup2 = stGroup.unload()
      stGroup2 should not be stGroup
      stGroup.nativeGroup should not be stGroup2.nativeGroup
    }
  }

  "registerRenderer()" should "be immutable" in {
    val newRenderer = new AttributeRenderer[FloatWrapper] {
      def toString(o: FloatWrapper, formatString: String, local: Locale): String = {
        o.f.toString
      }
    }

    withTemplateGroup(TemplateGroup1) { stGroup =>
      val stGroup2 = stGroup.registerRenderer(newRenderer)
      stGroup2 should not be stGroup
      stGroup2.renderers should not be stGroup.renderers
    }
  }

  "instanceOf" should "fail when attempting to find a nonexistent template" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      val tTemplate = stGroup.instanceOf("bar")
      tTemplate shouldBe 'failure
    }
  }

  it should "succeed when attempting to find a valid template" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      val tTemplate = stGroup.instanceOf("foo")
      tTemplate shouldBe 'success
    }
  }

  it should "properly render a template in the group" in {
    withTemplateGroup(TemplateGroup1) { stGroup =>
      val template = stGroup.instanceOf("foo").get
      // TODO: Fix me
      template.addAttributes(Map(
        "names" -> List("a", "b", "c"),
        "values" -> List("1", "2", "3")
      ))

      template.render() shouldBe "(a,1) (b,2) (c,3)"
    }
  }

  it should "properly handle an alternate encoding" in {
    withTemplateGroup(TemplateGroup2, "UTF-8") { stGroup =>
      val template = stGroup.instanceOf("bar").get
      // TODO: Fix me
      val args = Seq("one", "two")
      template.addAttributes(Map("args" -> args))
      template.render() shouldBe args.mkString("\u2014")
    }
  }

  it should "properly handle importing from a subdirectory" in {
    withTemplateGroup(TemplateGroup3) { stGroup =>
      for ((sep, name) <- Seq((" ", "quux"), ("-", "blah"))) {
        val t = stGroup.instanceOf(name)
        t shouldBe 'success
        val template = t.get
        // TODO: Fix me
        val args = Seq("one", "two", "three")
        template.addAttributes(Map("args" -> args))
        template.render() shouldBe args.mkString(sep)
      }
    }
  }
}

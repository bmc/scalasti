package org.clapper.scalasti

import java.io._
import java.net.URL
import java.util.Locale

import grizzled.file.util.{joinPath, withTemporaryDirectory, dirname}
import grizzled.util.withResource
import grizzled.util.CanReleaseResource.Implicits.CanReleaseCloseable

// Can't register renderers for primitive types.
class FloatWrapper(val f: Float)

/** Tests for STGroup class
  */
class STGroupFileSpec extends BaseSpec {

  val TemplateGroup1 = ("foo.stg",
      s"""|delimiters "%", "%"
          |import "parens.st"
          |import "foo.st"
          |""".stripMargin
  )

  val Templates1 = Seq(
    "foo.st" ->
      """foo(names, values) ::= <<
        |%names, values:{ n,v | %parens([n, v])%}; separator=" "%
        |>>""".stripMargin,
    "parens.st" ->
      """parens(args) ::= <<
        |(%args; separator=","%)
        |>>""".stripMargin
  )

  val TemplateGroup2 = ("bar.stg",
    """|delimiters "$", "$"
       |import "bar.st"
    """.stripMargin
  )

  val Templates2 = Seq(
    "bar.st" ->
      """bar(args) ::= <<
        |$args;separator="\u2014"$
        |>>""".stripMargin
  )

  val TemplateGroup3 = ("baz.stg",
    """|delimiters "$", "$"
       |import "subdir"
    """.stripMargin
  )

  val Templates3 = Seq(
    "subdir/quux.st" ->
    """quux(args) ::= <<
      |$args;separator=" "$
      |>>""".stripMargin,

    "subdir/blah.st" ->
      """blah(args) ::= <<
        |$args;separator="-"$
        |>>""".stripMargin
  )

  "load()" should "fail on a nonexistent file" in {
    val grp = STGroupFile(new URL("file:///tmp/foo.stg"))
    grp.load() shouldBe 'failure
  }

  it should "work when pointed at a legit file" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
      stGroup.load() shouldBe 'success
    }
  }

  it should "be immutable" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
      val t = stGroup.load()
      t shouldBe 'success
      val stGroup2 = t.get
      stGroup2 should not be stGroup
      stGroup.nativeGroup should not be stGroup2.nativeGroup
    }
  }

  "unload()" should "be immutable" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
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

    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
      val stGroup2 = stGroup.registerRenderer(newRenderer)
      stGroup2 should not be stGroup
      stGroup2.renderers should not be stGroup.renderers
    }
  }

  "instanceOf" should "fail when attempting to find a nonexistent template" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
      val tTemplate = stGroup.instanceOf("bar")
      tTemplate shouldBe 'failure
    }
  }

  it should "succeed when attempting to find a valid template" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
      val tTemplate = stGroup.instanceOf("foo")
      tTemplate shouldBe 'success
    }
  }

  it should "properly render a template in the group" in {
    withTemplateGroup(TemplateGroup1, Templates1) { (_, stGroup) =>
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
    withTemplateGroup(TemplateGroup2, Templates2, "UTF-8") { (_, stGroup) =>
      val template = stGroup.instanceOf("bar").get
      // TODO: Fix me
      val args = Seq("one", "two")
      template.addAttributes(Map("args" -> args))
      template.render() shouldBe args.mkString("\u2014")
    }
  }

  it should "properly handle importing from a subdirectory" in {
    withTemplateGroup(TemplateGroup3, Templates3) { (_, stGroup) =>
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

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  private def withTemplateGroup(group: (String, String),
                                templates: Seq[(String, String)],
                                encoding: String = "ASCII")
                               (code: (File, STGroup) => Unit): Unit = {
    withTemporaryDirectory("scalasti") { dir =>

      val (pathString, groupString) = group
      val groupPath = new File(joinPath(dir.getPath, pathString))

      def makeParentDir(path: File) = {
        val dir = dirname(path.getPath)
        if ((dir != ".") && (dir != "..")) {
          val fDir = new File(dir)
          fDir.mkdirs()
          if (! fDir.exists)
            throw new RuntimeException(s"Can't create $dir")
        }
      }

      def openWithEncoding(path: File, encoding: String) = {
        makeParentDir(path)
        new PrintWriter(
          new OutputStreamWriter(new FileOutputStream(path), encoding)
        )
      }

      withResource(openWithEncoding(groupPath, encoding)) { out =>
        out.println(groupString)
      }

      for ((path, templateString) <- templates) {
        val templatePath = new File(joinPath(dir.getPath, path))
        withResource(openWithEncoding(templatePath, encoding)) { out =>
          out.println(templateString)
        }
      }

      code(groupPath, STGroupFile(groupPath.toURI.toURL))
    }
  }
}

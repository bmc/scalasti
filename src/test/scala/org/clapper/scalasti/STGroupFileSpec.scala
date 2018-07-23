package org.clapper.scalasti

import java.net.URL


/** Tests for STGroup class
  */
class STGroupFileSpec extends BaseSpec {

  val TemplateGroup1 = TemplateGroupFileData(
    path = "foo.stg",
    groupString = s"""|delimiters "%", "%"
                      |import "foo.st"
                      |""".stripMargin,
    templates = Seq(
      TemplateData(
        path = "foo.st",
        templateString = """|foo(names, values) ::= <<
                            |%names,values: {n,v | %n%-%v%}; separator=","%
                            |>>""".stripMargin
      )
    )
  )

  val TemplateGroup2 = TemplateGroupFileData(
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

  val TemplateGroup3 = TemplateGroupFileData(
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
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      stGroup.load() shouldBe 'success
    }
  }

  it should "be immutable" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val t = stGroup.load()
      t shouldBe 'success
      val stGroup2 = t.get
      stGroup2 should not be theSameInstanceAs (stGroup)
      stGroup.nativeGroup should not be theSameInstanceAs (stGroup2.nativeGroup)
    }
  }

  "unload()" should "be immutable" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val stGroup2 = stGroup.unload()
      stGroup2 should not be stGroup
      stGroup.nativeGroup should not be theSameInstanceAs (stGroup2.nativeGroup)
    }
  }

  "registerRenderer()" should "be immutable" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val stGroup2 = stGroup.registerRenderer(floatRenderer)
      stGroup2 should not be theSameInstanceAs (stGroup)
      stGroup2.attrRenderers should not be theSameInstanceAs (stGroup.attrRenderers)
    }
  }

  "registerRenderer()" should "not lose templates on multiple calls" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val stGroup2 = stGroup
        .registerRenderer(floatRenderer)
        .registerRenderer(intRenderer)

      stGroup2.instanceOf("foo") shouldBe 'success
      stGroup2.fileName shouldBe stGroup.fileName
    }
  }

  "instanceOf" should "fail when attempting to find a nonexistent template" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val tTemplate = stGroup.instanceOf("bar")
      tTemplate shouldBe 'failure
    }
  }

  it should "succeed when attempting to find a valid template" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val tTemplate = stGroup.instanceOf("/foo")
      tTemplate shouldBe 'success
    }
  }

  it should "properly render a template in the group" in {
    withTemplateGroupFile(TemplateGroup1) { stGroup =>
      val template = stGroup.instanceOf("foo").get
      val template2 = template.addAttributes(Map(
        "names" -> List("a", "b", "c"),
        "values" -> List("1", "2", "3")
      ))

      val ts = template2.render()
      ts shouldBe 'success
      ts.get shouldBe "a-1,b-2,c-3"
    }
  }

  it should "properly handle an alternate encoding" in {
    withTemplateGroupFile(TemplateGroup2, "UTF-8") { stGroup =>
      val template = stGroup.instanceOf("bar").get
      val args = Seq("one", "two")

      val template2 = template.addAttributes(Map("args" -> args))
      val ts = template2.render()
      ts shouldBe 'success
      ts.get shouldBe args.mkString("\u2014")
    }
  }

  it should "properly handle importing from a subdirectory" in {
    withTemplateGroupFile(TemplateGroup3) { stGroup =>
      for ((sep, name) <- Seq((" ", "quux"), ("-", "blah"))) {
        val t = stGroup.instanceOf(name)
        t shouldBe 'success
        val template = t.get
        val args = Seq("one", "two", "three")
        val template2 = template.addAttributes(Map("args" -> args))
        val ts = template2.render()
        ts shouldBe 'success
        ts.get shouldBe args.mkString(sep)
      }
    }
  }

  "apply()" should "be able to read a group from a URL" in {
    // Assumes existence of resources under src/test/resources

    val resourceURL = getClass.getClassLoader.getResource("t1/group1.stg")
    val grp = STGroupFile(resourceURL)
    val tST = grp.instanceOf("template1")
    tST shouldBe 'success
    val st = tST.get.add("s", "Foo")
    st.render() shouldBe 'success
  }

  it should "handle imported templates from a URL" in {
    // Assumes that src/test/resources/t1/group1.stg does an import of
    // template2.st

    val resourceURL = getClass.getClassLoader.getResource("t1/group1.stg")
    val grp = STGroupFile(resourceURL)
    val tST = grp.instanceOf("template2")
    tST shouldBe 'success
    val st = tST.get.add("s", "Foo")
    st.render() shouldBe 'success
  }

  it should "find a file in the classpath, if a local file isn't found" in {
    // Assumes existence of resources under src/test/resources

    val grp = STGroupFile("t1/group1.stg")
    val tST = grp.instanceOf("template2")
    tST shouldBe 'success
    val st = tST.get.add("s", "Foo")
    st.render() shouldBe 'success
  }
}

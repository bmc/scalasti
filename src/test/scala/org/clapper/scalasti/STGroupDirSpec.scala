package org.clapper.scalasti

/** Tests for STGroupDir.
  */
class STGroupDirSpec extends BaseSpec {

  val TemplateGroupDir = TemplateGroupDirData(
    path = "my-group",
    templates = Seq(
      TemplateData(
        path = "foo.st",
        templateString =
          """|foo(args) ::= <<
             |<args; separator="-">
             |>>""".stripMargin
      )
    )
  )

  "apply()" should "be able to load a directory from a URL" in {
    val url = getClass.getClassLoader.getResource("t1")
    val grp = STGroupDir(url)
    val tST = grp.instanceOf("template2")
    tST shouldBe 'success
  }

  "load()" should "fail on a nonexistent directory" in {
    val grp = STGroupDir("/tmp/foo/bar/baz")
    grp.load() shouldBe 'failure
  }

  it should "not fail on an existing directory" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      grp.load() shouldBe 'success
    }
  }

  it should "be immutable" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      val t = grp.load()
      t shouldBe 'success
      val grp2 = t.get
      grp2 should not be theSameInstanceAs (grp)
      grp.nativeGroup should not be theSameInstanceAs (grp2.nativeGroup)
    }
  }

  "unload()" should "be immutable" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      val grp2 = grp.unload()
      grp2 should not be theSameInstanceAs (grp)
      grp.nativeGroup should not be theSameInstanceAs (grp2.nativeGroup)
    }
  }


  "instanceOf" should "fail when attempting to find a nonexistent template" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      val tTemplate = grp.instanceOf("bar")
      tTemplate shouldBe 'failure
    }
  }

  it should "succeed when attempting to find a valid template" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      val tTemplate = grp.instanceOf("foo")
      tTemplate shouldBe 'success
    }
  }

  it should "properly render a template in the group" in {
    withTemplateGroupDir(TemplateGroupDir) { grp =>
      val template = grp.instanceOf("foo").get
      val args = Seq("lkjasdf", "dkldka asdl", "foobar", "dkkdkkdkkd")
      val template2 = template.addAttributes(Map("args" -> args))

      template2 should renderSuccessfullyAs (args.mkString("-"))
    }
  }
}

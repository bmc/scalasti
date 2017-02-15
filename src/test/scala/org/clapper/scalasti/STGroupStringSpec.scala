package org.clapper.scalasti

/** STGroupString tests
  */
class STGroupStringSpec extends BaseSpec {

  val TemplateGroup1 =
    """|delimiters "%", "%"
       |
       |foo(args) ::= <<FOO='%args; separator=","%'>>
    """.stripMargin

  "load()" should "always be a no-op" in {
    val grp = STGroupString(TemplateGroup1)
    grp.load() shouldBe 'success
  }

  it should "be immutable" in {
    val grp = STGroupString(TemplateGroup1)
    val grp2 = grp.load().get
    grp2 should not be theSameInstanceAs (grp)
    grp.nativeGroup should not be theSameInstanceAs (grp2.nativeGroup)
  }

  "unload()" should "be immutable" in {
    val grp = STGroupString(TemplateGroup1)
    val grp2 = grp.unload()
    grp2 should not be theSameInstanceAs (grp)
    grp.nativeGroup should not be theSameInstanceAs (grp2.nativeGroup)
  }

  "instanceOf" should "fail when attempting to find a nonexistent template" in {
    val grp = STGroupString(TemplateGroup1)
    grp.instanceOf("bar") shouldBe 'failure
  }

  it should "succeed when attempting to find a valid template" in {
    val grp = STGroupString(TemplateGroup1)
    grp.instanceOf("foo") shouldBe 'success
  }

  it should "properly render a template in the group" in {
    val grp = STGroupString(TemplateGroup1)
    val template = grp.instanceOf("foo").get
    val args = Seq("lkjasdf", "dkldka asdl", "foobar", "dkkdkkdkkd")
    val template2 = template.addAttributes(Map("args" -> args))

    template2 should renderSuccessfullyAs (s"FOO='${args.mkString(",")}'")
  }
}

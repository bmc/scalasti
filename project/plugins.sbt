resolvers ++= Seq("less is" at "http://repo.lessis.me")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
      url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
          Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

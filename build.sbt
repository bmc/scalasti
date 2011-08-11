// TO DO: Add Markdown doc processing back in.

// ---------------------------------------------------------------------------
// Basic settings

name := "scalasti"

organization := "org.clapper"

version := "0.5.4"

scalaVersion := "2.8.1"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked")

crossScalaVersions := Seq("2.9.0-1", "2.9.0", "2.8.1", "2.8.0")

// ---------------------------------------------------------------------------
// Additional repositories

resolvers ++= Seq(
    "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
)

// ---------------------------------------------------------------------------
// ScalaTest dependendency

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
    // Select ScalaTest version based on Scala version
    val scalatestVersionMap = Map("2.8.0"   -> ("scalatest", "1.3"),
                                  "2.8.1"   -> ("scalatest_2.8.1", "1.5.1"),
                                  "2.9.0"   -> ("scalatest_2.9.0", "1.6.1"),
                                  "2.9.0-1" -> ("scalatest_2.9.0-1", "1.6.1"))
    val (scalatestArtifact, scalatestVersion) = scalatestVersionMap.getOrElse(
        sv, error("Unsupported Scala version: " + scalaVersion)
    )
    deps :+ "org.scalatest" % scalatestArtifact % scalatestVersion % "test"
}

fork in Test := true

// ---------------------------------------------------------------------------
// Posterous-SBT

libraryDependencies <<= (sbtVersion, scalaVersion, libraryDependencies) { (sbtv, scalav, deps) =>
    if (scalav == "2.8.1")
        deps :+ "net.databinder" %% "posterous-sbt" % ("0.3.0_sbt" + sbtv)
    else
        deps
}

(name in Posterous) := "Scalasti"

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper" %% "grizzled-scala" % "1.0.7",
    "org.clapper" %% "classutil" % "0.3.7",
    "org.antlr" % "stringtemplate" % "3.2.1"
)

// ---------------------------------------------------------------------------
// Publishing criteria

publishTo <<= version {(v: String) =>
    val nexus = "http://nexus.scala-tools.org/content/repositories/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "snapshots/") 
    else                             Some("releases"  at nexus + "releases/")
}

publishMavenStyle := true

credentials += Credentials(Path.userHome / "src" / "mystuff" / "scala" /
                           "nexus.scala-tools.org.properties")

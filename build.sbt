// TO DO: Add Markdown doc processing back in.

// ---------------------------------------------------------------------------
// Basic settings

name := "scalasti"

organization := "org.clapper"

version := "1.1.0"

licenses := Seq(
  "BSD" -> url("http://software.clapper.org/scalasti/license.html")
)

homepage := Some(url("http://software.clapper.org/scalasti/"))

description := (
  "A Scala-friendly wrapper for Terence Parr's StringTemplate library"
)

scalaVersion := "2.10.2"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

crossScalaVersions := Seq("2.10.2")

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq(
  "template", "string template", "StringTemplate"
)

(description in LsKeys.lsync) <<= description(d => d)

// ---------------------------------------------------------------------------
// Additional repositories

resolvers ++= Seq(
    "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
)

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper" %% "grizzled-scala" % "1.1.2",
    "org.clapper" %% "classutil" % "1.0.1",
    "org.antlr" % "stringtemplate" % "3.2.1",
    "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test"
)

// ---------------------------------------------------------------------------
// Publishing criteria

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:bmc/scalasti.git/</url>
    <connection>scm:git:git@github.com:bmc/scalasti.git</connection>
  </scm>
  <developers>
    <developer>
      <id>bmc</id>
      <name>Brian Clapper</name>
      <url>http://www.clapper.org/bmc</url>
    </developer>
  </developers>
)

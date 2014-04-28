// TO DO: Add Markdown doc processing back in.

// ---------------------------------------------------------------------------
// Basic settings

name := "scalasti"

organization := "org.clapper"

version := "2.0.0"

licenses := Seq(
  "BSD" -> url("http://software.clapper.org/scalasti/license.html")
)

homepage := Some(url("http://software.clapper.org/scalasti/"))

description := (
  "A Scala-friendly wrapper for Terence Parr's ST library"
)

scalaVersion := "2.10.3"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

crossScalaVersions := Seq("2.10.3", "2.11.0")

lsSettings

(LsKeys.tags in LsKeys.lsync) := Seq(
  "template", "string template", "ST"
)

(description in LsKeys.lsync) <<= description(d => d)

bintraySettings

bintray.Keys.packageLabels in bintray.Keys.bintray := (
  LsKeys.tags in LsKeys.lsync
).value

// ---------------------------------------------------------------------------
// Additional repositories

resolvers ++= Seq(
    "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
)

// ---------------------------------------------------------------------------
// ScalaTest dependendency


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper" %% "grizzled-scala" % "1.2",
    "org.clapper" %% "classutil" % "1.0.4",
    "org.antlr" % "ST4" % "4.0.8"
)

// ---------------------------------------------------------------------------
// Publishing criteria

// Don't set publishTo. The Bintray plugin does that automatically.

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

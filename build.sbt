// ---------------------------------------------------------------------------
// Basic settings

name         := "scalasti"
organization := "org.clapper"
version      := "3.0.1"

licenses := Seq(
  "BSD" -> url("http://software.clapper.org/scalasti/license.html")
)

homepage := Some(url("http://software.clapper.org/scalasti/"))

description := "A Scala-friendly wrapper for Terence Parr's ST library"

crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.6")

scalaVersion := crossScalaVersions.value.head

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
bintrayPackageLabels := Seq("library", "string-template", "template")
incOptions := incOptions.value.withNameHashing(true)

// ---------------------------------------------------------------------------
// Additional repositories

resolvers ++= Seq(
  "Java.net Maven 2 Repo" at "http://download.java.net/maven/2",
  "Bintray bmc" at "https://dl.bintray.com/bmc/maven"
)

// ---------------------------------------------------------------------------
// ScalaTest dependendency

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper"    %% "grizzled-scala" % "4.2.0",
    "org.clapper"    %% "classutil"      % "1.1.2",
    "org.antlr"       % "ST4"            % "4.0.8"
)

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

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
      <url>https://github.com/bmc</url>
    </developer>
  </developers>
)

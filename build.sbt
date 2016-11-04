// ---------------------------------------------------------------------------
// Basic settings

name         := "scalasti"
organization := "org.clapper"
version      := "2.1.1"

licenses := Seq(
  "BSD" -> url("http://software.clapper.org/scalasti/license.html")
)

homepage := Some(url("http://software.clapper.org/scalasti/"))

description := "A Scala-friendly wrapper for Terence Parr's ST library"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0")

scalaVersion := crossScalaVersions.value.head

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
bintrayPackageLabels := Seq("library", "string-template", "template")
incOptions := incOptions.value.withNameHashing(true)

// ---------------------------------------------------------------------------
// Additional repositories

resolvers ++= Seq(
    "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
)

// ---------------------------------------------------------------------------
// ScalaTest dependendency

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies ++= Seq(
    "org.clapper"    %% "grizzled-scala" % "3.1.0",
    "org.clapper"    %% "classutil"      % "1.1.0",
    "org.antlr"       % "ST4"            % "4.0.8"
)

libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
  deps :+ "org.scala-lang" % "scala-reflect" % sv
}

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

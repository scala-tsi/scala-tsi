import sbt.Keys.sourceGenerators

lazy val root = (project in file("."))
  .enablePlugins(nl.codestar.scala.ts.plugin.TypescriptGenPlugin)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.4"
  )

//libraryDependencies += "nl.codestar" % "scala-ts-compiler" % "0.1-SNAPSHOT"

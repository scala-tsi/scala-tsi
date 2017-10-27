import sbt.Keys.sourceGenerators

lazy val root = (project in file("."))
  .enablePlugins(nl.codestar.scala.ts.plugin.TypescriptGenPlugin)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.4"//,
    //sourceGenerators in Compile += (generateTypescriptGeneratorApplication in Compile)
  )

libraryDependencies += "nl.codestar" % "scala-tsi-sbt" % "0.1-SNAPSHOT"

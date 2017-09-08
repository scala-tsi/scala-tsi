import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtTestOnCompile
import sbt.Keys._

lazy val root = (project in file("."))
.settings(
  Seq(
    scalaVersion := "2.12.3",
    organization := "nl.codestar",
    scalacOptions ++= compilerOptions,
    scalafmtOnCompile in Compile := true,
    scalafmtTestOnCompile in Compile := true,
    inputDirectory in Compile := (scalaSource in Compile).value / "models"
  ),
  libraryDependencies += "org.clapper" %% "classutil" % "1.1.2"
)

(compile in Compile) := ((compile in Compile) dependsOn (typescript in Compile)).value

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding", "UTF8",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-language:experimental.macros"
)




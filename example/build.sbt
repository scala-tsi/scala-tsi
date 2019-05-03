import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.0-RC1",
      organization := "nl.codestar",
      scalacOptions ++= compilerOptions,
      typescriptClassesToGenerateFor := Seq("Foo"),
      typescriptGenerationImports := Seq("models._", "Foo._"),
      scalafmtConfig := file("../.scalafmt.conf"),
      scalafmtOnCompile := true  // format code on compile
    )
  )

// (compile in Compile) := ((compile in Compile) dependsOn (typescript in Compile)).value

lazy val compilerOptions = Seq(
  "-Xsource:2.13",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "UTF8",
  "-target:jvm-1.8",
)

resolvers += Opts.resolver.sonatypeReleases
resolvers += Opts.resolver.sonatypeSnapshots

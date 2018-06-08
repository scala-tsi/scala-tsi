import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.12.6",
      organization := "nl.codestar",
      scalacOptions ++= compilerOptions,
      typescriptClassesToGenerateFor := Seq("Foo"),
      typescriptGenerationImports := Seq("models._", "Foo._")
      //scalafmtOnCompile in Compile := true,
      //scalafmtTestOnCompile in Compile := true
    )
  )

// (compile in Compile) := ((compile in Compile) dependsOn (typescript in Compile)).value

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "UTF8",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-language:experimental.macros"
)

resolvers += Opts.resolver.sonatypeReleases
resolvers += Opts.resolver.sonatypeSnapshots

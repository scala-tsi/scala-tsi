import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.2",
      organization := "nl.codestar",
      scalacOptions ++= compilerOptions,
      typescriptClassesToGenerateFor := Seq("Foo", "Sealed", "Person"),
      typescriptGenerationImports := Seq("models._", "ReadmeTSTypes._"),
      typescriptOutputFile := baseDirectory.value / "model.ts",
      scalafmtConfig := file("../.scalafmt.conf")
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
  "-target:jvm-1.8"
)

//resolvers += Opts.resolver.sonatypeReleases
//resolvers += Opts.resolver.sonatypeSnapshots

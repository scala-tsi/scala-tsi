import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.2",
      organization := "com.scala-tsi",
      scalacOptions ++= compilerOptions,
      typescriptClassesToGenerateFor := Seq("Foo", "Sealed", "Person", "DeepThought"),
      typescriptGenerationImports := Seq("models._", "ReadmeTSTypes._"),
      typescriptOutputFile := baseDirectory.value / "model.ts",
      scalafmtConfig := file("../.scalafmt.conf")
      // Enable to debug macros
      // scalacOptions += "-Ymacro-debug-lite"
    )
  )

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "UTF8",
  "-target:jvm-1.8"
)

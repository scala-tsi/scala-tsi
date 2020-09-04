import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.3",
      organization := "com.scalatsi",
      scalacOptions ++= compilerOptions,
      typescriptExports := Seq("Foo", "Sealed", "Person", "DeepThought", "ScalaEnum.type", "JavaEnum"),
      typescriptGenerationImports := Seq("models._", "ReadmeTSTypes._", "models.enumeration._"),
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
  "UTF8"
)

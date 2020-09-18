import sbt.Keys._

ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.3",
      organization := "com.scalatsi",
      scalacOptions ++= compilerOptions,
      // Enable to debug macros
      // scalacOptions += "-Ymacro-debug-lite",
      typescriptExports := Seq("Foo", "Sealed", "DiscriminatedUnion", "Person", "DeepThought", "ScalaEnum.type", "JavaEnum", "Integer"),
      typescriptGenerationImports := Seq("models._", "ReadmeTSTypes._", "models.enumeration._", "DiscriminatedUnion.TSTypes._"),
      typescriptOutputFile := baseDirectory.value / "model.ts",
      scalafmtConfig := file("../.scalafmt.conf")
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

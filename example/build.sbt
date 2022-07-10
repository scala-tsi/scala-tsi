import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion := "2.13.8",
      organization := "com.scalatsi",
      scalacOptions ++= compilerOptions,
      typescriptExports := Seq(
        "DeepThought",
        "GenericCaseClass",
        "GreetFunction",
        "Greeter",
        "Integer",
        "JavaEnum",
        "MyCaseClass",
        "ScalaEnum.type",
        "Sealed",
      ),
      typescriptGenerationImports        := Seq("models._", "ReadmeTSTypes._", "models.enumeration._"),
      typescriptOutputFile               := baseDirectory.value / "model.ts",
      typescriptTaggedUnionDiscriminator := Some("kind"),
      scalafmtConfig                     := file("../.scalafmt.conf")
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

import sbt.Keys._

val defaultScalaVersion = "2.13.10"

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion := sys.props.get("scala.version").getOrElse(defaultScalaVersion),
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

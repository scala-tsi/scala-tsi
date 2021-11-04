import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion                := "2.13.7",
      organization                := "com.scalatsi",
      typescriptExports           := Seq("DeepNestingTopLevel", "ParentObj"),
      typescriptGenerationImports := Seq("models._", "models.TSTypes._"),
      typescriptOutputFile        := baseDirectory.value / "model.ts",
      scalafmtConfig              := file("../../../../.scalafmt.conf")
    )
  )

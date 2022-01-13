import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion                := "2.13.8",
      organization                := "com.scalatsi",
      typescriptExports           := Seq("DeepNestingTopLevel", "NestedGenerated"),
      typescriptGenerationImports := Seq("models._"),
      typescriptOutputFile        := baseDirectory.value / "model.ts",
      scalafmtConfig              := file("../../../../.scalafmt.conf")
    )
  )

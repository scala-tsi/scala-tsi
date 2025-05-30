import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion                := sys.props.get("scala.version").get,
      organization                := "com.scalatsi",
      typescriptExports           := Seq("DeepNestingTopLevel", "NestedGenerated"),
      typescriptGenerationImports := Seq("models._"),
      typescriptOutputFile        := baseDirectory.value / "model.ts",
      scalafmtConfig              := file("../../../../.scalafmt.conf"),
      scalacOptions               := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq()
        case _            => Seq("-Xmax-inlines", "64")
      })
    )
  )

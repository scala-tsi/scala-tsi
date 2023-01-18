import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion                := sys.props.get("scala.version").get,
      organization                := "com.scalatsi",
      typescriptExports           := Seq("ClassWithUndefinedMember"),
      typescriptGenerationImports := Seq("models._"),
      typescriptOutputFile        := baseDirectory.value / "model.ts",
      scalafmtConfig              := file("../../../../.scalafmt.conf")
    )
  )

import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion         := sys.props.get("scala.version").get,
      typescriptOutputFile := baseDirectory.value / "model.ts"
    )
  )

import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion         := "2.13.10",
      typescriptOutputFile := baseDirectory.value / "model.ts"
    )
  )

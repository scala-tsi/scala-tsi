import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion         := "2.13.9",
      typescriptOutputFile := baseDirectory.value / "model.ts"
    )
  )

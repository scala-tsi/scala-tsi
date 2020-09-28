import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    Seq(
      scalaVersion := "2.13.3",
      typescriptOutputFile := baseDirectory.value / "model.ts"
    )
  )

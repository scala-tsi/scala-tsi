import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion := sys.props.get("scala.version").get,
      organization := "com.scalatsi",
      // TODO: Enable the recursive type for Scala 3: https://github.com/scala-tsi/scala-tsi/issues/278
      typescriptExports := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("ClassWithUndefinedMember", "RecursiveA")
        case _            => Seq("ClassWithUndefinedMember")
      }),
      typescriptGenerationImports := Seq("models._"),
      typescriptOutputFile        := baseDirectory.value / "model.ts",
      scalafmtConfig              := file("../../../../.scalafmt.conf"),
      TaskKey[Unit]("copyExpectedModel") := {
        val scalaV = CrossVersion.partialVersion(scalaVersion.value).get._1
        val source = baseDirectory.value / s"expected_model_scala$scalaV.ts"
        val target = baseDirectory.value / s"expected_model.ts"
        IO.copy(Seq((source, target)))
      }
    )
  )

import sbt.Keys._

val scala3   = "3.3.6"
val scala213 = "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(ScalaTsiPlugin)
  .settings(
    Seq(
      scalaVersion       := sys.props.get("scala.version").getOrElse(scala213),
      crossScalaVersions := Seq(scala213, scala3),
      organization       := "com.scalatsi",
      compilerOptions,
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
      scalafmtConfig                     := file("../.scalafmt.conf"),
      resolvers += Resolver.mavenLocal
    ),
  )

lazy val compilerOptions = scalacOptions := Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding",
  "UTF8"
) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, _)) =>
    Seq(
      "-Xsource:3",
      "-Xlint"
    )
  case _ =>
    Seq(
      "-explain",
    )
})

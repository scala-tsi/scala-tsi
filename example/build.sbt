import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(TypescriptGenPlugin)
  .settings(
    Seq(
      scalaVersion := "2.12.3",
      organization := "nl.codestar",
      scalacOptions ++= compilerOptions,
      typescriptClassesToGenerateFor := Seq("Foo"),
      typescriptGenerationImports := Seq("models._")
      //scalafmtOnCompile in Compile := true,
      //scalafmtTestOnCompile in Compile := true
    ),
    libraryDependencies ++= Seq(
      "org.clapper" %% "classutil" % "1.1.2"
    )
  )

// (compile in Compile) := ((compile in Compile) dependsOn (typescript in Compile)).value

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding", "UTF8",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-language:experimental.macros"
)




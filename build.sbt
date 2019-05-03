import sbt.Keys.scalacOptions

lazy val commonSettings = Seq(
  organization := "nl.codestar",
  version := "0.1.4-SNAPSHOT",
  scalacOptions ++= compilerOptions,
  scalafmtOnCompile := true, // format code on compile
  scalaVersion := "2.13.0-RC1",
  crossScalaVersions := Seq("2.12.8", "2.13.0-RC1")
)

/* Settings to publish to maven central */
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  credentials += (sys.env.get("MAVEN_CENTRAL_USER") match {
    case Some(user) => Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, sys.env("MAVEN_CENTRAL_PASSWORD"))
    case None       => Credentials(Path.userHome / ".ivy2" / ".nl-codestar-maven-central-credentials")
  }),
  licenses := Seq("MIT" -> url("https://github.com/code-star/scala-tsi/blob/master/LICENSE")),
  homepage := Some(url("https://github.com/code-star/scala-tsi")),
  scmInfo := Some(ScmInfo(url("https://github.com/code-star/scala-tsi"), "scm:git@github.com:code-star/scala-tsi.git")),
  developers := List(
    Developer(id = "dhoepelman", name = "David Hoepelman", email = "dhoepelman@gmail.com", url = url("https://github.com/dhoepelman")),
    Developer(id = "donovan", name = "Donovan de Kuiper", email = "donovan.de.kuiper@ordina.nl", url = url("https://github.com/Hayena"))
  )
)

lazy val compilerOptions = Seq(
  "-Xsource:2.14",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "UTF8",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Ywarn-unused:params,-implicits",
  "-language:experimental.macros"
) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 13)) => Seq()
  case Some((2, 12)) => Seq(
    "-Yno-adapted-args",
  )
})


 //
lazy val `scala-tsi-macros` = (project in file("macros"))
  .settings(
    commonSettings,
    name := "scala-tsi-macros",
    description := "Macros for scala-tsi",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    // Disable publishing
    publish := {},
    publishLocal := {}
  )

lazy val `scala-tsi` = (project in file("."))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "scala-tsi",
    description := "Generate Typescript interfaces from your scala classes",
    libraryDependencies ++= Seq(
      // Cross-build library for 2.13 and 2.12 collections
      "org.scala-lang.modules" %% "scala-collection-compat" % "1.0.0",
      // testing framework
      "org.scalatest" %% "scalatest" % "3.0.8-RC2" % "test"
    )
  )
  // Depend and include the macro project, instead of having to publish a separate macro project
  .dependsOn(`scala-tsi-macros` % "compile-internal, test-internal")
  .settings(
    // Add dependencies from the macro project
    libraryDependencies ++= (`scala-tsi-macros` / libraryDependencies).value,
    // include the macro classes and resources in the main jar
    mappings in (Compile, packageBin) ++= mappings
      .in(`scala-tsi-macros`, Compile, packageBin)
      .value,
    // include the macro sources in the main source jar
    mappings in (Compile, packageSrc) ++= mappings
      .in(`scala-tsi-macros`, Compile, packageSrc)
      .value
  )

lazy val `sbt-scala-tsi` = (project in file("plugin"))
  .enablePlugins(SbtTwirl, BuildInfoPlugin)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "sbt-scala-tsi",
    description := "SBT plugin to generate Typescript interfaces from your scala classes as part of your build",
    sbtPlugin := true,
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "sbt.info",
    // sbt 1 uses scala 2.12
    scalaVersion := "2.12.8",
    crossScalaVersions := Seq("2.12.8"),
  )

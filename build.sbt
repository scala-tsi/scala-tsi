//import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtTestOnCompile
import sbt.Keys.scalacOptions

// TODO: Different versions between 2.11 and 2.12
lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "UTF8",
  "-target:jvm-1.8",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Ywarn-unused:params,-implicits",
  "-language:experimental.macros"
) //++
// Scala 2.11 only settings
// Seq("-Ydelambdafy:method", "-Ybackend:GenBCode","-Xsource:2.12", "-Ywarn-unused", "-Ywarn-unused-import")

lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "nl.codestar",
  version := "0.1.3-SNAPSHOT",
  scalacOptions ++= compilerOptions
  // Code formatting
  //scalafmtOnCompile in Compile := true,
  //scalafmtTestOnCompile in Compile := true
)

/* Settings to publish to maven central */
lazy val publishSettings = Seq(
  // format: off
  publishMavenStyle := true,
  publishTo := Some(if(isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  credentials += (sys.env.get("MAVEN_CENTRAL_USER") match {
    case Some(user) => Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, sys.env("MAVEN_CENTRAL_PASSWORD"))
    case None => Credentials(Path.userHome / ".ivy2" / ".nl-codestar-maven-central-credentials")
  }),
  licenses := Seq("MIT" -> url("https://github.com/code-star/scala-tsi/blob/master/LICENSE")),
  homepage := Some(url("https://github.com/code-star/scala-tsi")),
  scmInfo := Some(ScmInfo(url("https://github.com/code-star/scala-tsi"), "scm:git@github.com:code-star/scala-tsi.git")),
  developers := List(
    Developer(id="dhoepelman", name="David Hoepelman", email="david.hoepelman@ordina.nl", url=url("https://github.com/dhoepelman")),
    Developer(id="donovan", name="Donovan de Kuiper", email="donovan.de.kuiper@ordina.nl", url=url("https://github.com/Hayena"))
  )
  // format: on
)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

lazy val `scala-tsi-macros` = (project in file("macros"))
  .settings(
    commonSettings,
    name := "scala-tsi-macros",
    description := "Macros for scala-tsi",
    libraryDependencies += scalaReflect.value,
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
    libraryDependencies := Seq(
      "org.scalatest"        %% "scalatest"    % "3.0.5"     % "test"
    )
  )
  // Depend and include the macro project, instead of having to publish a separate macro project
  .dependsOn(`scala-tsi-macros` % "compile-internal, test-internal")
  .settings(
    // Add a dependency to scala-reflect from the macro project
    libraryDependencies += scalaReflect.value,
    // include the macro classes and resources in the main jar
    mappings in (Compile, packageBin) ++= mappings.in(`scala-tsi-macros`, Compile, packageBin).value,
    // include the macro sources in the main source jar
    mappings in (Compile, packageSrc) ++= mappings.in(`scala-tsi-macros`, Compile, packageSrc).value
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
    buildInfoPackage := "sbt.info"
  )

import sbt.Keys.scalacOptions
import sbt.ScriptedPlugin.autoImport.scriptedBufferLog
import xerial.sbt.Sonatype._
import com.jsuereth.sbtpgp.PgpKeys.publishLocalSigned

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3   = "3.3.6"
val scala213 = "2.13.17"
val scala212 = "2.12.17" // for sbt

val isScala2 = settingKey[Boolean]("isScala2")

lazy val commonSettings = Seq(
  organization       := "com.scalatsi",
  version            := "0.8.4-SNAPSHOT",
  scalaVersion       := scala3,
  crossScalaVersions := Seq(scala213, scala3),
  compilerOptions,
  isScala2 := (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) => true
    case _            => false
  })
)

/* Settings to publish to maven central */
lazy val publishSettings = Seq(
  versionScheme     := Some("early-semver"),
  publishMavenStyle := true,
  publishTo         := sonatypePublishToBundle.value,
  credentials ++= sys.env
    .get("MAVEN_CENTRAL_USER")
    .map(user => Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, sys.env("MAVEN_CENTRAL_PASSWORD"))))
    .getOrElse(Seq()),
  sonatypeProfileName    := "com.scalatsi",
  licenses               := Seq("MIT" -> url("https://github.com/scala-tsi/scala-tsi/blob/master/LICENSE")),
  sonatypeProjectHosting := Some(GitHubHosting("scala-tsi", "scala-tsi", "992153+dhoepelman@users.noreply.github.com")),
) ++
  // CI-only settings, enabled if $CI env variable is set to "true"
  sys.env
    .get("CI")
    .collect({ case "true" =>
      Seq(
        usePgpKeyHex("6044257F427C2854A6F9A0C211A02377A6DD0E59"),
        pgpSecretRing := file(".circleci/circleci.key.asc"),
        pgpPublicRing := file(".circleci/circleci.pub.asc"),
        pgpPassphrase := sys.env.get("GPG_passphrase").map(_.toCharArray),
      )
    })
    .getOrElse(Seq())

lazy val compilerOptions = scalacOptions := Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding",
  "UTF8",
  "-release",
  "8",
  "-Xfatal-warnings",
) ++ (if (isScala2.value)
        Seq(
          "-language:experimental.macros",
          "-Xsource:3",
          // These are not yet implemented in Scala 3 compiler
          "-Xlint",
          "-Ywarn-numeric-widen",
          "-Ywarn-value-discard",
          "-Ywarn-dead-code",
          "-Ywarn-unused:implicits",
          "-Ywarn-unused:imports",
          "-Ywarn-unused:locals",
          "-Ywarn-unused:params",
          "-Ywarn-unused:patvars",
          "-Ywarn-unused:privates",
        )
      else
        Seq(
          "-explain",
          // Only enable during development of macros, this will lead to extra runtime overhead
          // "-Xcheck-macros",
        ))

/** ***************
  * scala-tsi and macros
  * **************
  */

lazy val `scala-tsi-macros` = (project in file("macros"))
  .settings(
    commonSettings,
    name        := "scala-tsi-macros",
    description := "Macros for scala-tsi, Scala 2",
    libraryDependencies ++= (if (isScala2.value) Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
                             else Seq.empty),
    scalaVersion       := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    // Disable publishing
    publish        := {},
    publishLocal   := {},
    publish / skip := true,
  )

lazy val `scala-tsi` = (project in file("."))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(scalatsiSettings)
  // Scala 2 needs a separate compilation step and thus separate project. Scala 3 doesn't need any of this at all.
  .dependsOn(`scala-tsi-macros` % "compile-internal, test-internal")
  .settings(
    // Add dependencies from the macro project
    libraryDependencies ++= (if (isScala2.value) (`scala-tsi-macros` / libraryDependencies).value else Seq.empty),
    // include the macro classes and resources in the main jar
    Compile / packageBin / mappings ++=
      (if (isScala2.value) (`scala-tsi-macros` / Compile / packageBin / mappings).value else Seq.empty),
    // include the macro sources in the main source jar
    Compile / packageSrc / mappings ++=
      (if (isScala2.value) (`scala-tsi-macros` / Compile / packageSrc / mappings).value else Seq.empty)
  )

lazy val scalaTsiPublishLocal = (`scala-tsi` / publishLocal).scopedKey
val pub                       = taskKey[Unit]("publish locally for testing")
pub      := (`sbt-scala-tsi` / publishLocal).value
scripted := (`sbt-scala-tsi` / scripted).evaluated

lazy val scalatsiSettings = Seq(
  name        := "scala-tsi",
  description := "Generate Typescript interfaces from your scala classes",
  libraryDependencies ++= Seq(
    // testing framework
    "org.scalatest" %% "scalatest" % "3.2.19" % "test"
  )
)

/** ***************
  * sbt-scala-tsi
  * **************
  */

lazy val `sbt-scala-tsi` = (project in file("plugin"))
  .enablePlugins(SbtTwirl, BuildInfoPlugin, SbtPlugin)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name             := "sbt-scala-tsi",
    description      := "SBT plugin to generate Typescript interfaces from your scala classes as part of your build",
    sbtPlugin        := true,
    buildInfoKeys    := Seq[BuildInfoKey](version),
    buildInfoPackage := "sbt.info",
    // sbt 1 uses scala 2.12
    scalaVersion       := scala212,
    crossScalaVersions := Seq(scala212),
    // Twirl template gives an incorrect unused import warning
    scalacOptions := scalacOptions.value diff Seq("-Xlint", "-Ywarn-unused:imports"),
    publishLocal  := publishLocal.dependsOn(scalaTsiPublishLocal).value
  )
  .settings(
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq(
        "-Xmx1024M",
        "-Dplugin.version=" + version.value,
        "-Dscala.version=" + sys.props.get("scala.version").getOrElse("\"Pass -Dscala.version=....\"")
      )
    },
    scriptedBufferLog := false,
    // Make sure to publish the library locally first
    scripted := scripted.dependsOn(publishLocal).evaluated
  )

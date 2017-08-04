name := "scala-ts-compiler"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.11"

// Options for all scala versions
scalacOptions ++= Seq(
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
  "-Xfatal-warnings"
)

// scala 2.11 only
scalacOptions ++= Seq("-Ydelambdafy:method", "-Ybackend:GenBCode","-Xsource:2.12", "-Ywarn-unused", "-Ywarn-unused-import")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt"     % "3.2.0",
  "org.scalatest"    %% "scalatest" % "3.0.1" % "test"
)

addCommandAlias("generate-typescript", "runMain GenerateTypescript")

// Code formatting
scalafmtOnCompile in Compile := true
scalafmtTestOnCompile in Compile := true

// code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

// Templating engine for the generator application
addSbtPlugin("com.typesafe.play" % "sbt-twirl" % "1.5.2")

// To enable publishing to maven central
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.15")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.2.1")

// Enable version infromation in the build
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

// To test our own SBT plugin
libraryDependencies += { "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value }

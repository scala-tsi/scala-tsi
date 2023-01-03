// code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

// Templating engine for the generator application
addSbtPlugin("com.typesafe.play" % "sbt-twirl" % "1.5.2")

// To enable publishing to maven central
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.6")
addSbtPlugin("com.jsuereth"   % "sbt-pgp"      % "1.1.2-1")

// Enable version infromation in the build
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

// To test our own SBT plugin
libraryDependencies += { "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value }

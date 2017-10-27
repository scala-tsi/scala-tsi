addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.11")

lazy val root = Project("plugin", file(".")).dependsOn(ProjectRef(plugin, "plugin"))

lazy val plugin = file("../").getCanonicalFile.toURI

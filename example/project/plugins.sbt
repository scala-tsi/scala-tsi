addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.14")

sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("nl.codestar" % "scala-tsi-sbt" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}


/*

lazy val root = Project("plugin", file(".")).dependsOn(ProjectRef(plugin, "scala-tsi-sbt"))

lazy val plugin = file("../").getCanonicalFile.toURI

*/

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

lazy val scala_tsi_version = {
  val v = sys.env
    .get("PLUGIN_VERSION")
    .orElse(sys.props.get("plugin.version"))
    .getOrElse("0.3.2")
  ConsoleLogger().info(s"Using scala-tsi $v")
  v
}

addSbtPlugin("com.scalatsi" % "sbt-scala-tsi" % scala_tsi_version)

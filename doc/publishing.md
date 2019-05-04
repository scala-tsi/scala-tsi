# Publishing

# Locally

`sbt +publishLocal +sbt-scala-tsi/publishLocal`

you can now use the published version locally in other projects

# Maven Central

```
sbt +publishSigned +sbt-scala-tsi/publishSigned
// releases everything from nl.codestar, look into releasing only this project
sbt "sonatypeReleaseAll nl.codestar"
```

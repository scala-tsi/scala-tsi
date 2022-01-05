# Plugin tests

This directory contains projects that are automatically run in CI as test cases for the SBT plugin.
The expected output is in `expected_model.ts` of each subdirectory.

### Running

First publish scala-tsi and the plugin locally from the repository root:
```sh
sbt +scala-tsi/publishLocal sbt-scala-tsi/publishLocal
```

Then generate the typescript from the test:
```sh
cd plugin/src/sbt-test/ScalaTsiPlugin/xxxxxx
sbt -Dplugin.version=0.X.X-SNAPSHOT generateTypescript
diff model.ts expected_model.ts
```
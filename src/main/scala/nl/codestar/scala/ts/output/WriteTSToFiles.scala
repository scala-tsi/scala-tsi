package nl.codestar.scala.ts.output

import java.io.FileWriter

import nl.codestar.scala.ts.interface.TypescriptType.TypescriptNamedType
import nl.codestar.scala.ts.interface.TypescriptTypeSerializer

object WriteTSToFiles {
  def write(options: OutputOptions)(types: Seq[TypescriptNamedType]): Unit = {
    val output = TypescriptTypeSerializer.emits(types: _*)
    if (!options.targetFile.exists()) {
      options.targetFile.createNewFile()
    }
    val writer = new FileWriter(options.targetFile)
    writer.write(output)
    writer.close()
  }
}

package nl.codestar.scalatsi.output

import java.io.FileWriter

import nl.codestar.scalatsi.TypescriptType.TypescriptNamedType
import nl.codestar.scalatsi.TypescriptTypeSerializer

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

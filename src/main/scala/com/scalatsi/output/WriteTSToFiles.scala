package com.scalatsi.output

import java.io.{FileWriter, IOException}
import com.scalatsi.{TypescriptType, TypescriptNamedType}

import scala.util.Try

object WriteTSToFiles {
  def generate(options: OutputOptions)(types: Map[String, TypescriptType]): Unit = {
    val namedTypes = types.values.collect {
      case named: TypescriptNamedType => named
    }.toSet
    val unnamedTypes = types.filterNot(_.isInstanceOf[TypescriptNamedType])

    // Tell the user the unnamed types will be ignored


    write(options)(namedTypes)
  }

  private def write(options: OutputOptions)(types: Set[TypescriptNamedType]): Unit = {
    try {
      val targetFile = options.targetFile
      val output     = TypescriptTypeSerializer.emits(options.styleOptions, types)

      Try {
        Option(targetFile.getParentFile).foreach(_.mkdirs())
        targetFile.createNewFile()
      } // createNewFile will continue if file exists
        .recover {
          case e: SecurityException => logger.exit(s"Could not create file '$targetFile' due to JVM security stopping it", code = 2, e = e)
          case e: IOException       => logger.exit(s"Could not create file '$targetFile' due to I/O problem", code = 2, e = e)
        }.get

      // TODO: For some reason scala.util.Using isn't working in 2.12, even though we have the compat library
      //      Using(new FileWriter(targetFile)) { writer =>
      //        writer.write(output)
      //      }.recover {
      //        case e: IOException => reportFailure(s"Could not write typescript to file '$targetFile' due to I/O problem", code = 2, e = e)
      //      }.get
      (for {
        writer <- Try(new FileWriter(targetFile))
        _ <- Try {
          try { writer.write(output) }
          finally { writer.close() }
        }
      } yield ()).recover {
        case e: IOException => logger.exit(s"Could not write typescript to file '$targetFile' due to I/O problem", code = 2, e = e)
      }.get

      ()
    } catch {
      case e: Throwable =>
        logger.exit(
          """Uncaught exception in scala-tsi output writer.
            |Please file a bug report at https://github.com/scala-tsi/scala-tsi/issues""".stripMargin,
          e = e
        )
    }
  }
}

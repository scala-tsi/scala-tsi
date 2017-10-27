package nl.codestar.scala.ts

import better.files._
import java.io.{File => JFile}

object WriteTSToFiles {

  case class Config(
      outputFile: Option[File] = None
  ) {
    def output(content: String): Unit = outputFile match {
      case Some(file) =>
        file.overwrite(content)
        ()
      case None => System.out.println(content)
    }
  }

  val optionParser =
    new scopt.OptionParser[Config]("generateTypescriptFiles") {
      head("generateTypescriptFiles")

      opt[JFile]('f', "outputFile")
        .optional()
        .valueName("<file>")
        .action((v, conf) => conf.copy(outputFile = Some(v.toScala)))

//      opt[File]('o', "outputDir")
//        .optional()
//        .valueName("<dir>")
//        .action((v, conf) => conf.copy(outputDir = Some(v.toPath)))
//        .text("set and output directory to emit the typescript files in per interface")

      help("help").text("print this usage text")

//      checkConfig(
//        conf =>
//          if (conf.outputDir.isDefined && conf.outputFile.isDefined)
//            failure("Cannot output to both file and directory")
//          else success)
    }

}

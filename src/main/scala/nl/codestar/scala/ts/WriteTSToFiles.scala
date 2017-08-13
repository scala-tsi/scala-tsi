package nl.codestar.scala.ts

import java.io.{File, OutputStream}
import java.nio.file.{Files, Path, Paths}

import nl.codestar.scala.ts.interface.DefaultTSTypes

import scala.util.{Failure, Success, Try}

object WriteTSToFiles extends App {
  import DefaultTSTypes._

  case class Config(
                     outputFile: Option[Path] = None,
                     outputDir: Option[Path] = None
                   ) {
    def outputToDirectory: Boolean = outputDir.isDefined

    private def outDir = outputDir.get
    private lazy val mkOutDir: Boolean = Files.exists(outDir) match {
      case true if Files.isDirectory(outDir) => true
      case true =>
        System.err.println(s"$outDir is not a directory")
        sys.exit(1)
      case false if Files.createDirectories(outDir) => true
      case false =>
        System.err.println(s"Could not create output directory $outDir")
        sys.exit(1)

    }

    def outputStream(tsName: String): OutputStream = Try {
      outputDir match {
        case Some(_) =>
          mkOutDir
          val file = outDir.resolve(tsName + ".ts")
          Files.createFile(file)
          Files.newOutputStream(file)
        case None =>
          //TODO
      }
    } match {
      case Success(s) => s
      case Failure(t) =>
        System.err.println(s"Could not create outputstream for file $tsName")
        t.printStackTrace(System.err)
        sys.exit(1)
    }
  }

  val optionParser = new scopt.OptionParser[Config]("generateTypescriptFiles") {
    head("generateTypescriptFiles")

    opt[File]('f', "outputFile").optional().valueName("<file>")
        .action( (v, conf) => conf.copy(outputFile = Some(v.toPath)))

    opt[File]('o', "outputDir").optional().valueName("<dir>")
      .action( (v, conf) => conf.copy(outputDir = Some(v.toPath)))
      .text("set and output directory to emit the typescript files in per interface")

    help("help").text("print this usage text")

    checkConfig( conf =>
      if(conf.outputDir.isDefined && conf.outputFile.isDefined) failure("Cannot output to both file and directory")
      else success
    )
  }


}

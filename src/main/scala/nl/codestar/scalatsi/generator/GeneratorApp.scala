package nl.codestar.scalatsi.generator

object GeneratorApp {
  def main(args: Array[String]): Unit = {
    _root_.nl.codestar.scalatsi.output.WriteTSToFiles.write(UserOptions.value)(UserClasses.outputClasses)
  }
}

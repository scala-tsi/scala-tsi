package nl.codestar.scalatsi.generator

object Generator {
  def main(args: Array[String]): Unit = {
    _root_.nl.codestar.scalatsi.output.WriteTSToFiles.write(options)(toOutput)
  }

  val options = _root_.nl.codestar.scalatsi.output.OutputOptions(
    targetLocation = new File("@targetLocation")
  )

  def main(args: Array[String]): Unit = {

  }
}

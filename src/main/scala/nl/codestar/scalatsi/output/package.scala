package nl.codestar.scalatsi

import com.scalatsi.{output => newname}

package object output {
  @deprecated("0.3.0", "Use com.scalatsi.output.OutputOptions")
  type OutputOptions = newname.OutputOptions
  @deprecated("0.3.0", "Use com.scalatsi.output.StyleOptions")
  type StyleOptions = newname.StyleOptions
  @deprecated("0.3.0", "Use com.scalatsi.output.WriteTSToFiles")
  final val WriteTSToFiles = newname.WriteTSToFiles
}

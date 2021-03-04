package com.scalatsi.output

import java.io.File

case class OutputOptions(
  targetFile: File,
  styleOptions: StyleOptions = StyleOptions(),
  header: Option[String]
)

case class StyleOptions(
  semicolons: Boolean = false
) {
  private[scalatsi] val sc: String = if (semicolons) ";" else ""
}

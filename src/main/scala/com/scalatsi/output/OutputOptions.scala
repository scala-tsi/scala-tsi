package com.scalatsi.output

import java.io.File

case class OutputOptions(
    targetFile: File,
    styleOptions: StyleOptions = StyleOptions(),
    header: Option[String] = None
)

case class StyleOptions(
    semicolons: Boolean = false,
    taggedUnionDiscriminator: Option[String] = Some("type")
) {
  private[scalatsi] val sc: String = if (semicolons) ";" else ""
}

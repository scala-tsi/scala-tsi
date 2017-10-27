@(tsi: TypescriptInterface[_], moduleName: String)

@tsi.imports.map(_.toString).mkString("\n")

module @{moduleName} {
  'use strict';

  @tsi.content.replace("\n\n", "\n").replace("\n", "\n  ")
}

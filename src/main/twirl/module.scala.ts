@(tsi: TypescriptInterface[_ <: AnyRef], moduleName: String)

@es6imports(tsi.imports)

module @{moduleName} {
  'use strict';

  @tsi.content.replace("\n\n", "\n").replace("\n", "\n  ")
}

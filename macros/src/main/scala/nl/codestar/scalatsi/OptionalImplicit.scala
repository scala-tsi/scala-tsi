package nl.codestar.scalatsi

/** Utility for checking whether a mapping is available
*
* It is necessary if we want to provide default implicit arguments, because macro calls cannot have default arguments
* I.e. this is not allowed
* def getOrGenerate[T](implicit mapping: TSType[T] = null) = macro ...
* So instead we do
* def getOrGenerate[T](implicit mapping: OptionalImplicit[T, TSType]) = macro ...
*
* @see [[TSType.optionalImplicitTSType]] object  in the main project
* */
case class OptionalImplicit[T](value: Option[T])

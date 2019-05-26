package nl.codestar.scalatsi

import scala.reflect.macros.blackbox

private[scalatsi] class MacroUtil[C <: blackbox.Context](val c: C) {

  private[this] var lookingUpList = List[c.Type]()

  // looking up implicits ourselves requires us to do our own error and divergence checking
  def safeLookupOptionalImplicit(T: c.Type): Option[c.Tree] = {

    val orLookingUpList = lookingUpList

    val found = try {
      if (orLookingUpList.exists(alreadyLookingUp => T <:< alreadyLookingUp)) {
        println(s"Aborting on $T")
        // We've entered this type before => we've entered a recursive loop and must stop
        c.universe.EmptyTree
      } else {
        lookingUpList = T :: orLookingUpList
        // look up implicit type, return EmptyTree if not found
        c.inferImplicitValue(T, silent = true)
      }
    } catch {
      case e: Exception =>
        c.abort(c.enclosingPosition, s"Encountered exception: $e")
        c.universe.EmptyTree
    } finally {
      lookingUpList = orLookingUpList
    }

    Option(found)
      .filter(_ != c.universe.EmptyTree)
  }
}

package nl.codestar.scalatsi

import scala.reflect.macros.blackbox

/** Generic Macro utility not really related to specific scala-tsi logic */
private[scalatsi] class MacroUtil[C <: blackbox.Context](val c: C) {

  private[this] var lookingUpList = List[c.Type]()

  // looking up implicits ourselves requires us to do our own error and divergence checking
  def lookupOptionalImplicit(T: c.Type): Option[c.Tree] = {

    val orLookingUpList = lookingUpList

    val found = try {
      if (orLookingUpList.exists(alreadyLookingUp => T <:< alreadyLookingUp)) {
        // We've entered this type before => we've entered a recursive loop and must stop
        c.universe.EmptyTree
      } else {
        lookingUpList = T :: orLookingUpList
        // look up implicit type, return EmptyTree if not found
        c.inferImplicitValue(T, silent = true)
      }
    } catch {
      case _: Exception =>
        c.universe.EmptyTree
    } finally {
      lookingUpList = orLookingUpList
    }

    Option(found)
      .filter(_ != c.universe.EmptyTree)
  }

  def lookupOptionalGenericImplicit[T: c.WeakTypeTag, F[_]](implicit tsTypeTag: c.WeakTypeTag[F[_]]): Option[c.Tree] = {
    // Get the T => F[T] function
    val typeConstructor = c.weakTypeOf[F[_]].typeConstructor
    // Construct the F[T] type we need to look up
    val lookupType = c.universe.appliedType(typeConstructor, c.weakTypeOf[T])
    lookupOptionalImplicit(lookupType)
  }
}

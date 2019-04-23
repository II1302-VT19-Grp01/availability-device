package utils

import database.{HasId, Id}
import play.api.mvc.PathBindable

/**
  * Extra Play path- and query string binders for custom types.
  */
object Binders {
  implicit def idPathBindable[A <: HasId](
                                           implicit inner: PathBindable[A#IdType]
                                         ): PathBindable[Id[A]] = inner.transform(Id(_), _.raw)
}
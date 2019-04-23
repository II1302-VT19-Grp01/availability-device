package database

/**
  * A type-safe wrapper for ID values.
  *
  * This gives us the ability to refer to an `Id[User]` instead of remembering
  * that [[User]]s have [[scala.Long]] IDs.
  *
  * It also teaches the compiler that an `Id[User]` is completely unrelated to
  * an `Id[UserSession]`, even if they have the same underlying type.
  */
case class Id[A <: HasId](raw: A#IdType)

/**
  * Marker trait for types that define an ID.
  */
trait HasId {
  def id: Id[Self]

  /**
    * The class implementing [[HasId]].
    */
  type Self >: this.type <: HasId

  /**
    * The underlying type of the ID, such as [[scala.Long]].
    */
  type IdType
}
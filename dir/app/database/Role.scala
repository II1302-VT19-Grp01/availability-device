package database

import utils.Enum

/**
  * A privilege "level" of a user. See `docs/Roles.org` for more details.
  */
sealed trait Role extends Ordered[Role] {
  def compare(that: Role): Int =
    Role.values.indexOf(this) - Role.values.indexOf(that)
}

/**
  * A non-anonymous [[Role]] that can be associated with an actual user.
  */
sealed trait UserRole extends Role
object Role extends Enum[Role] {
  case object Anonymous extends Role
  case object Applicant extends UserRole
  case object Employee  extends UserRole
  case object Admin     extends UserRole

  /**
    * The minimum role of a registered user
    */
  val User            = Applicant
  override def values = Seq(Anonymous) ++ UserRole.values
}
object UserRole extends Enum[UserRole] {
  override def values = Seq(Role.Applicant, Role.Employee, Role.Admin)
}
package database

import PgProfile.api._

// case class works as a DTO, taking the required columns from the database
/**
  * A user account.
  */
case class User(id: Id[User],
                username: Option[String],
                password: Option[String],
                firstname: Option[String],
                surname: Option[String],
                email: Option[String],
                role: UserRole = Role.Applicant)
  extends HasId {
  type Self = User
  type IdType = Long
}

/**
  * SLICK scala code instead of raw SQL, allowing low coupling
  * Selecting the columns from the table
  */
class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Id[User]]("id", O.PrimaryKey, O.AutoInc)

  def username = column[Option[String]]("username")

  def password = column[Option[String]]("password")

  def firstname = column[Option[String]]("firstname")

  def surname = column[Option[String]]("surname")

  def email = column[Option[String]]("email")

  def role = column[UserRole]("role")

  /**
    * These columns make up the User object
    */
  override def * =
    (id, username, password, firstname, surname, email, role) <> (User.tupled, User.unapply)
}

/**
  * Refering to this query when we use the object Users
  */
object Users extends TableQuery[Users](new Users(_))
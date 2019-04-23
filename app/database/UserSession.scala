package database

import PgProfile.api._
import java.time.Instant

/**
  * A single session of a logged-in user.
  */
case class UserSession(id: Id[UserSession],
                       user: Id[User],
                       from: Instant,
                       refreshed: Instant,
                       deleted: Boolean)
  extends HasId {
  type Self   = UserSession
  type IdType = Long
}

class UserSessions(tag: Tag) extends Table[UserSession](tag, "sessions") {
  def id        = column[Id[UserSession]]("id", O.PrimaryKey, O.AutoInc)
  def userId    = column[Id[User]]("user")
  def from      = column[Instant]("from")
  def refreshed = column[Instant]("refreshed")
  def deleted   = column[Boolean]("deleted")

  def user = foreignKey("user_fk", userId, Users)(_.id)

  override def * =
    (id, userId, from, refreshed, deleted) <> (UserSession.tupled, UserSession.unapply)
}

object UserSessions extends TableQuery[UserSessions](new UserSessions(_))
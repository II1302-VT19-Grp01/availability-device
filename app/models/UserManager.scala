package models

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

import com.google.inject.ImplementedBy
import database.PgProfile.api._
import database._
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Stores users and their sessions.
  */
@ImplementedBy(classOf[DbUserManager])
trait UserManager {
  def find(id: Id[User]): Future[Option[User]]

  /**
    * Finds the session by a given ID, as well as the associated user.
    */
  def findSession(id: Id[UserSession]): Future[Option[(User, UserSession)]]

  /**
    * Finds the user by a given username, verifies the password, and then creates
    * a session if the user was found.
    */
  def login(username: String, password: String): Future[Option[UserSession]]

  /**
    * Tries to create a user with the given fields.
    *
    * Registration MAY return more than one error at once, but this behaviour
    * MUST NOT be relied upon.
    *
    * @return Left(reasons) if the user creation failed, otherwise Right(session)
    */
  def register(
                username: String,
                password: String,
                firstname: String,
                surname: String,
                email: String
              ): Future[Either[Seq[UserManager.RegistrationError], UserSession]]
}

object UserManager {

  /**
    * A reason that the registration failed.
    */
  sealed trait RegistrationError
  object RegistrationError {

    /**
      * Registration failed because the chosen username conflicts with an
      * existing user's.
      */
    case object UsernameTaken extends RegistrationError

    /**
      * Registration failed because the email address conflicts with an existing
      * user's.
      */
    case object EmailTaken extends RegistrationError
  }
}

/**
  * Stores users and their sessions in the database.
  */
class DbUserManager @Inject()(
                               implicit protected val dbConfigProvider: DatabaseConfigProvider,
                               passwordHasher: PasswordHasher,
                               executionContext: ExecutionContext
                             ) extends UserManager
  with HasDatabaseConfigProvider[PgProfile] {
  private val logger = Logger(getClass)

  override def find(id: Id[User]): Future[Option[User]] = db.run {
    Users.filter(_.id === id).result.headOption
  }

  override def findSession(
                            id: Id[UserSession]
                          ): Future[Option[(User, UserSession)]] = db.run {
    for {
      session <- (for {
        session <- UserSessions
        if session.id === id
        if !session.deleted
        if session.refreshed > Instant.now().minus(1, ChronoUnit.DAYS)
        user <- session.user
      } yield (user, session)).result.headOption
      _ <- UserSessions
        .filter(_.id === session.map(_._2.id))
        .map(_.refreshed)
        .update(Instant.now())
    } yield session
  }

  override def login(username: String,
                     password: String): Future[Option[UserSession]] = {
    val task = db.run {
      (for {
        user <- Users
          .filter(user => user.username === username)
          .result
          .headOption
          .map(
            _.filter(
              user =>
                user.password
                  .map(passwordHasher.compare(_, password))
                  .getOrElse(false)
            )
          )
        session <- DBIO.sequenceOption(
          user.map(
            user =>
              UserSessions
                .map(_.userId)
                .returning(UserSessions) += user.id
          )
        )
      } yield session).transactionally
    }
    task.foreach {
      case Some(session) =>
        logger.info(s"User $username logged in with session ${session.id.raw}")
      case None =>
        logger.info(s"User $username failed to log in")
    }
    task
  }

  private def guessRegistrationFailureReasons(username: String, email: String) =
    DBIO
      .sequence(
        Seq(
          Users
            .filter(_.username === username)
            .result
            .headOption
            .map(_.map(_ => UserManager.RegistrationError.UsernameTaken)),
          Users
            .filter(_.email === email)
            .result
            .headOption
            .map(_.map(_ => UserManager.RegistrationError.EmailTaken))
        )
      )
      .map(_.flatten)

  override def register(
                         username: String,
                         password: String,
                         firstname: String,
                         surname: String,
                         email: String
                       ): Future[Either[Seq[UserManager.RegistrationError], UserSession]] = {
    val task = db.run {
      (for {
        userId <- Users.returning(Users.map(_.id)) += User(
          Id[User](-1),
          username = Some(username),
          password = Some(passwordHasher.hash(password)),
          firstname = Some(firstname),
          surname = Some(surname),
          email = Some(email)
        )
        session <- UserSessions.map(_.userId).returning(UserSessions) += userId
      } yield session).transactionally.asTry.flatMap {
        case Success(user) =>
          DBIO.successful(Right(user))
        case Failure(exception) =>
          guessRegistrationFailureReasons(username, email).asTry
            .map {
              case Success(Seq()) | Failure(_) =>
                // We can't find a good reason for this to fail,
                // so it was probably our faulet...
                throw exception
              case Success(userErrors) =>
                Left(userErrors)
            }
      }
    }
    task.foreach {
      case Right(session) =>
        logger.info(
          s"User $username was successfully created, with id ${session.user.raw}"
        )
      case Left(reason) =>
        logger.info(s"Failed to create $username: $reason")
    }
    task
  }
}
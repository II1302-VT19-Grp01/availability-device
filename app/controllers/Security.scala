package controllers

import database.{Id, Role, User, UserSession}
import models.UserManager
import play.api.libs.typedmap.TypedKey
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Maps database sessions to request sessions/cookies.
  */
trait SecurityHelpers {
  def getSessionId(request: RequestHeader): Option[Id[UserSession]] =
    request.session
      .get(Security.sessionKey)
      .map(id => Id[UserSession](id.toLong))

  def getUserSession(request: RequestHeader): Option[UserSession] =
    request.attrs
      .get(Security.session)
      .getOrElse(throw new NoSessionLoaderException())

  def getUser(request: RequestHeader): Option[User] =
    request.attrs
      .get(Security.user)
      .getOrElse(throw new NoSessionLoaderException)

  def clearUser(response: Result, request: RequestHeader): Result =
    response.removingFromSession(Security.sessionKey)(request)

  def setUserSessionId(response: Result,
                       request: RequestHeader,
                       sessionId: Id[UserSession]): Result =
    response.addingToSession(Security.sessionKey -> sessionId.raw.toString())(
      request
    )

  def setUserSession(response: Result,
                     request: RequestHeader,
                     session: UserSession): Result =
    setUserSessionId(response, request, session.id)

  /**
    * Add a dummy (lack of) session to the request, without requiring any
    * database access.
    *
    * This is used by the error templates to avoid causing a loop of
    * `failed to connect to database => failed to render error page => (start over)`.
    */
  def addNoUserToRequest(request: RequestHeader): RequestHeader =
    request
      .addAttr(Security.user, None)
      .addAttr(Security.session, None)

  implicit class UserReqHeader(private val req: RequestHeader) {
    def user: Option[User]               = getUser(req)
    def userSession: Option[UserSession] = getUserSession(req)
    def userRole: Role                   = user.map(_.role).getOrElse(Role.Anonymous)
    def loggedIn: Boolean                = user.isDefined
  }
}

/**
  * Internal error that is thrown when showing user session information without
  * having loaded it first.
  *
  * Seeing this exception means that you need to use [[Security.userAction]]
  * instead of Play's [[play.api.mvc.Action]].
  */
class NoSessionLoaderException
  extends Exception(
    "attempted to access session before it was loaded, use Security.userAction or checkUser instead of Action"
  )

/**
  * Mixin that extends [[SecurityHelpers]] with Controller-specific utilities,
  * such as action transformers.
  */
trait Security extends SecurityHelpers {
  protected def userManager: UserManager
  protected def Action: ActionBuilder[Request, AnyContent]

  def checkUser(implicit ec: ExecutionContext) =
    new ActionTransformer[Request, Request] {
      override def executionContext = ec
      override def transform[A](request: Request[A]) =
        findUser(request).map(
          session =>
            request
              .addAttr(Security.user, session.map(_._1))
              .addAttr(Security.session, session.map(_._2))
        )
    }

  def requireRole(role: Role)(implicit ec: ExecutionContext) =
    new ActionFilter[Request] {
      override def executionContext = ec
      override def filter[A](request: Request[A]) = Future.successful {
        if (request.userRole >= role) {
          None
        } else if (request.user.isEmpty) {
          Some(
            Results.Redirect(
              routes.LoginController.login(target = Some(request.uri))
            )
          )
        } else {
          Some(Results.Forbidden(views.html.error.forbidden()(request)))
        }
      }
    }

  def userAction(
                  requiredRole: Role = Role.Anonymous
                )(implicit ec: ExecutionContext) =
    requireRole(requiredRole) compose checkUser compose Action

  def findUser(
                request: RequestHeader
              )(implicit ec: ExecutionContext): Future[Option[(User, UserSession)]] =
    getSessionId(request) match {
      case Some(id) =>
        userManager.findSession(id)
      case None =>
        Future.successful(None)
    }
}

object Security extends SecurityHelpers {
  val user    = TypedKey[Option[User]]("user")
  val session = TypedKey[Option[UserSession]]("session")

  /**
    * The key used to store the session ID in the Play session
    */
  val sessionKey = "SESSION"
}
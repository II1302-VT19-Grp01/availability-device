package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models.InputManager
import models.UserManager

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's index page.
  */
@Singleton
class HomeController @Inject()(implicit val userManager: UserManager, cc: ControllerComponents, ec: ExecutionContext) extends AbstractController(cc) with Security {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    *
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = userAction().apply { implicit request: Request[AnyContent] =>
    // views.CustomEventPublish.main(Array())
    Ok(views.html.index())
  }


  def input() = userAction().apply { implicit request: Request[AnyContent] =>
    Ok(views.html.input())
  }

  def status() = userAction().apply { implicit request: Request[AnyContent] =>
    Ok(views.html.status())
  }

  def updateStatus() = userAction().apply { implicit request: Request[AnyContent] =>
    val inputMessage = request.body.asFormUrlEncoded.get.apply("statusInput").head
    InputManager.inputMessage = inputMessage
    Ok(views.html.index())
  }
}

package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models.InputManager

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's index page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    // views.CustomEventPublish.main(Array())
    Ok(views.html.index())
  }


  def input() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.input())
  }

  def status() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.status())
  }

  def updateStatus() = Action { implicit request: Request[AnyContent] =>
    val inputMessage = request.body.asFormUrlEncoded.get.apply("statusInput").head
    InputManager.inputMessage = inputMessage
    Ok(views.html.index())
  }
}

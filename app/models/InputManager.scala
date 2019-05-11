
package models

import javax.inject.Inject
import com.google.inject.ImplementedBy
import database.PgProfile.api._
import database._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global


@ImplementedBy(classOf[DbInputManager])
trait InputManager {
  def add(inputMessage: Option[String])

  def get(): Future[Option[String]]

  def getAll(): Future[Seq[String]]
}

class DbInputManager @Inject()(
                                protected val dbConfigProvider: DatabaseConfigProvider
                              ) extends InputManager
  with HasDatabaseConfigProvider[PgProfile] {
  override def add(inputMessage: Option[String]) = db.run {
    Inputs.map(_.message) += inputMessage
  }

  override def get() = db.run {
    Inputs.sortBy(_.id.desc).result.headOption.map(input => input.flatMap(_.message)) // .id is ascending on default.
  }

  override def getAll() = db.run { //gets all the messages from the database
    Inputs.sortBy(_.id.desc).result.map(input => input.flatMap(_.message))
  }


}




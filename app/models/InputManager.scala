
package models

/*import javax.inject.Inject
import com.google.inject.ImplementedBy
import database.PgProfile.api._
import database._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[DbInputManager])
trait InputManager {
  def String add(inputMessage)
}
 */

object InputManager {
  var inputMessage = ""
}

/*
class DbInputManager @Inject()(
                                protected val dbConfigProvider: DatabaseConfigProvider
                              ) extends InputManager
  with HasDatabaseConfigProvider[PgProfile] {
  override def add(inputMessage) = db.run {
    Inputs.map(_.message) += inputMessage
  }
}

*/


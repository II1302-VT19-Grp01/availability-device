package database

import PgProfile.api._


case class Input(id: Id[Input],
                 message: Option[String])
  extends HasId {
  type Self = Input
  type IdType = Long
}

/**
  * SLICK scala code instead of raw SQL, allowing low coupling
  * Selecting the columns from the table
  */
class Inputs(tag: Tag) extends Table[Input](tag, "inputs") {
  def id = column[Id[Input]]("id", O.PrimaryKey, O.AutoInc)

  def message = column[Option[String]]("message")


  /**
    * These columns make up the Input object
    */
  override def * =
    (id, message) <> (Input.tupled, Input.unapply)
}

/**
  * Refering to this query when we use the object Inputs
  */
object Inputs extends TableQuery[Inputs](new Inputs(_))
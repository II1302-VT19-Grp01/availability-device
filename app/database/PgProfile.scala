package database

import com.github.tminglei.slickpg.{
  ExPostgresProfile,
  PgDate2Support,
  PgEnumSupport
}

/**
  * Slick Profile that adds support for a few newer Postgres profiles that
  * aren't normally supported.
  */
trait PgProfile
  extends ExPostgresProfile
    with PgDate2Support
    with PgEnumSupport {
  trait API extends super.API with DateTimeImplicits {
    implicit def idColumnType[T <: HasId](
                                           implicit raw: ColumnType[T#IdType]
                                         ): ColumnType[Id[T]] = MappedColumnType.base[Id[T], T#IdType](_.raw, Id(_))

    implicit val userRoleTypeMapper =
      createEnumJdbcType[UserRole]("role",
        _.toString,
        UserRole.valueMap(_),
        quoteName = true)
    implicit val userRoleTypeListMapper =
      createEnumListJdbcType[UserRole]("role",
        _.toString,
        UserRole.valueMap(_),
        quoteName = true)
  }

  override val api = new API {}
}

object PgProfile extends PgProfile
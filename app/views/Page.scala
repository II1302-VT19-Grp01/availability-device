package views

/**
  * Used to track the current page for view-level concerns, such as marking the
  * * active page in the navbar.
  */
sealed trait Page
object Page {
  case object Home            extends Page
  case object Login           extends Page
  case object Register        extends Page
  case object Other           extends Page
}
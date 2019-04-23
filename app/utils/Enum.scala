package utils

/**
  * Marker type for "better" enums.
  *
  * Differs from [[scala.Enumeration]] in that it doesn't try to assign IDs
  * automatically, and doesn't prescribe where the values should come from.
  */
trait Enum[A] {
  def values: Seq[A]
  final def valueMap: Map[String, A] =
    values.map(a => a.toString() -> a).toMap
}
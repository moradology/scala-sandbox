package sandbox

import spray.json._

import java.util.UUID

case class MapAlgebraRef(operation: MapAlgebra, ref: UUID)

object MapAlgebraRef {
  import MapAlgebraProtocolSpray._
  implicit val mapAlgebraRefFormat = jsonFormat2(MapAlgebraRef.apply _)
}


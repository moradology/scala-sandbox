package sandbox

import geotrellis.raster.Tile
import geotrellis.vector.Polygon
import geotrellis.vector.io._

import spray.json._

import java.util.UUID

sealed trait MapAlgebra extends Product with Serializable {
  def args: List[MapAlgebra]
  def evaluable: Boolean
  def unbound: List[MapAlgebra]
}

object MapAlgebra {
  abstract class Operation(val symbol: String) extends MapAlgebra {
    def evaluable: Boolean = (args.length >= 1) && (args.foldLeft(true)(_ && _.evaluable))
    def unbound: List[MapAlgebra] =
      args.foldLeft(List[MapAlgebra]())({ case (list, mapAlgebra) =>
        if (!mapAlgebra.evaluable) mapAlgebra :: list
        else list
      })
  }

  case class Addition(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends Operation("+")
  case class Subtraction(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends Operation("-")
  case class Multiplication(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends Operation("*")
  case class Division(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends Operation("/")
  case class Masking(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends Operation("mask")
  case class Reclassification(args: List[MapAlgebra], id: UUID, label: Option[String], classBreaks: ClassBreaks)
      extends Operation("reclassify")


  sealed trait Source extends MapAlgebra {
    def args: List[MapAlgebra] = List.empty
    def unbound: List[MapAlgebra] = List(this)
  }

  sealed trait RasterSource extends Source
  case class RFSceneSource(id: UUID, label: Option[String], scene: Option[UUID], band: Option[Int])
      extends RasterSource { def evaluable = scene.isDefined }
  case class RFProjectSource(id: UUID, label: Option[String], project: Option[UUID], band: Option[Int])
      extends RasterSource { def evaluable = project.isDefined }
  case class MLToolSource(id: UUID, label: Option[String], toolRun: Option[UUID])
      extends RasterSource { def evaluable = toolRun.isDefined }
  case class RefSource(id: UUID, label: Option[String], ref: UUID)
      extends RasterSource { def evaluable = true }

  case class VectorSource(id: UUID, label: Option[String], value: Option[Polygon])
      extends Source { def evaluable = value.isDefined }
  case class DecimalSource(id: UUID, label: Option[String], value: Option[Double])
      extends Source { def evaluable = value.isDefined }
  case class IntegralSource(id: UUID, label: Option[String], value: Option[Int])
      extends Source { def evaluable = value.isDefined }
}


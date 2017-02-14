package sandbox

import geotrellis.raster.Tile
import geotrellis.vector.Polygon
import geotrellis.vector.io._

import spray.json._

import java.util.UUID

sealed trait MapAlgebra {
  def args: List[MapAlgebra]
}

object MapAlgebra {
  sealed trait Operation extends MapAlgebra { def symbol: String }

  sealed abstract class LocalOperation(val symbol: String) extends Operation
  case class Addition(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends LocalOperation("+")
  case class Subtraction(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends LocalOperation("-")
  case class Multiplication(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends LocalOperation("*")
  case class Division(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends LocalOperation("/")
  case class Masking(args: List[MapAlgebra], id: UUID, label: Option[String])
      extends LocalOperation("mask")
  case class Reclassification(args: List[MapAlgebra], id: UUID, label: Option[String], classBreaks: ClassBreaks)
      extends LocalOperation("reclassify")


  sealed trait Source extends MapAlgebra { def args: List[MapAlgebra] = List.empty }

  sealed trait RasterSource extends Source
  case class SceneSource(id: UUID, label: Option[String], scene: Option[UUID], band: Option[Int])
      extends RasterSource
  case class MosaicSource(id: UUID, label: Option[String], mosaic: Option[UUID], band: Option[Int])
      extends RasterSource
  case class MLToolSource(id: UUID, label: Option[String], toolRun: Option[UUID])
      extends RasterSource
  case class RefSource(id: UUID, label: Option[String], ref: UUID)
      extends RasterSource

  case class VectorSource(id: UUID, label: Option[String], value: Option[Polygon])
      extends Source
  case class DecimalSource(id: UUID, label: Option[String], value: Option[Double])
      extends Source
  case class IntegralSource(id: UUID, label: Option[String], value: Option[Int])
      extends Source
}

object MapAlgebraProtocol extends DefaultJsonProtocol {
  import MapAlgebra._
  object MapAlgebraJsonFormat extends JsonFormat[MapAlgebra] {
    def write(root: MapAlgebra): JsValue =
      root match {
        case op: Operation => op match {
          case lop: LocalOperation => lop match {
            case add@Addition(args, id, label) => JsObject(
              "apply" -> JsString(add.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson
            )
            case subtract@Subtraction(args, id, label) => JsObject(
              "apply" -> JsString(subtract.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson
            )
            case multiply@Multiplication(args, id, label) => JsObject(
              "apply" -> JsString(multiply.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson
            )
            case divide@Division(args, id, label) => JsObject(
              "apply" -> JsString(divide.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson
            )
            case mask@Masking(args, id, label) => JsObject(
              "apply" -> JsString(mask.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson
            )
            case reclassify@Reclassification(args, id, label, classBreaks) => JsObject(
              "apply" -> JsString(reclassify.symbol),
              "label" -> label.toJson,
              "args" -> args.toJson,
              "classBreaks" -> classBreaks.toJson
            )
          }
        }
        case src: Source => src match {
          case rs: RasterSource => rs match {
            case SceneSource(id, label, scene, band) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "scene" -> scene.toJson,
              "band" -> band.toJson,
              "type" -> JsString("raster")
            )
            case MosaicSource(id, label, mosaic, band) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "mosaic" -> mosaic.toJson,
              "band" -> band.toJson,
              "type" -> JsString("raster")
            )
            case MLToolSource(id, label, toolRun) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "toolRun" -> toolRun.toJson,
              "type" -> JsString("raster")
            )
            case RefSource(id, label, ref) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "ref" -> ref.toJson,
              "type" -> JsString("raster")
            )
          }
          case VectorSource(id, label, value) => JsObject(
            "id" -> id.toJson,
            "label" -> label.toJson,
            "value" -> value.toJson,
            "type" -> JsString("polygon")
          )
          case DecimalSource(id, label, value) => JsObject(
            "id" -> id.toJson,
            "label" -> label.toJson,
            "value" -> value.toJson,
            "type" -> JsString("double")
          )
          case IntegralSource(id, label, value) => JsObject(
            "id" -> id.toJson,
            "label" -> label.toJson,
            "value" -> value.toJson,
            "type" -> JsString("int")
          )
        }
      }

    def read(jsvalue: JsValue) = {
      val jsObj = jsvalue.asJsObject
      val keys = jsObj.fields.keys.toList

      // For reuse
      val nodeLabel: Option[String] = jsObj.getFields("label") match {
        case Seq(JsString(label)) => Some(label)
        case _ => None
      }
      val nodeId: UUID = jsObj.getFields("id") match {
        case Seq(id) => id.convertTo[UUID]
        case _ => deserializationError(s"Unable to deserialize node ID in $jsObj")
      }

      if (keys.contains("type")) { // This is a source
        jsObj.getFields("type") match {
          case Seq(JsString("raster")) =>
            if (keys.contains("mosaic")) {
              val (mosaic, band) = jsObj.getFields("mosaic", "band") match {
                case Seq(mosaic, band) => (Some(mosaic.convertTo[UUID]), Some(band.convertTo[Int]))
                case Seq(mosaic) => (Some(mosaic.convertTo[UUID]), None)
                case _ => deserializationError("Unable to deserialize Raster Foundry mosaic")
              }
              MosaicSource(nodeId, nodeLabel, mosaic, band)
            } else if (keys.contains("scene")) {
              val (scene, band) = jsObj.getFields("scene", "band") match {
                case Seq(scene, band) => (Some(scene.convertTo[UUID]), Some(band.convertTo[Int]))
                case Seq(scene) => (Some(scene.convertTo[UUID]), None)
                case _ => deserializationError("Unable to deserialize Raster Foundry scene")
              }
              SceneSource(nodeId, nodeLabel, scene, band)
            } else if (keys.contains("toolRun")) {
              val toolRun = jsObj.getFields("toolRun") match {
                case Seq(tRun) => tRun.convertTo[Option[UUID]]
                case _ => deserializationError("Unable to deserialize Model Lab tool run")
              }
              MLToolSource(nodeId, nodeLabel, toolRun)
            } else if (keys.contains("ref")) {
              val ref: UUID = jsObj.getFields("ref") match {
                case Seq(reference) => reference.convertTo[UUID]
                case _ => deserializationError("Unable to deserialize map algebra reference")
              }
              RefSource(nodeId, nodeLabel, ref)
            } else {
              println(s"${jsvalue}")
              deserializationError("Unrecognized raster source")
            }
          case Seq(JsString("polygon")) =>
            val poly = jsObj.getFields("value") match {
              case Seq(polygon) => Some(polygon.convertTo[Polygon])
              case _ => None
            }
            VectorSource(nodeId, nodeLabel, poly)
          case Seq(JsString("double")) =>
            val double = jsObj.getFields("value") match {
              case Seq(dbl) => Some(dbl.convertTo[Double])
              case _ => None
            }
            DecimalSource(nodeId, nodeLabel, double)
          case Seq(JsString("int")) =>
            val int = jsObj.getFields("value") match {
              case Seq(integer) => Some(integer.convertTo[Int])
              case _ => None
            }
            IntegralSource(nodeId, nodeLabel, int)
        }
      } else {                           // This is a function
        jsObj.getFields("apply", "args") match {
          case Seq(JsString("+"), args) => Addition(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
          case Seq(JsString("-"), args) => Subtraction(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
          case Seq(JsString("*"), args) => Multiplication(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
          case Seq(JsString("/"), args) => Division(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
          case Seq(JsString("mask"), args) => Masking(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
          case Seq(JsString("reclassify"), args) =>
            val classBreaks: ClassBreaks = jsObj.getFields("classBreaks") match {
              case Seq(cBreaks) => cBreaks.convertTo[ClassBreaks]
              case _ => deserializationError(s"Unable to deserialize classbreaks in ${jsvalue}")
            }
            Reclassification(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel, classBreaks)
        }
      }
    }
  }
  implicit val lazyMapAlgebraJsonFormat = lazyFormat[MapAlgebra](MapAlgebraJsonFormat)
}

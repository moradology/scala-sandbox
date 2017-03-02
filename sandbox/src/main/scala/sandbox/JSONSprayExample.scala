package sandbox

import geotrellis.raster.Tile
import geotrellis.vector.Polygon
import geotrellis.vector.io._
import geotrellis.raster.render._
import spray.json._

import java.util.UUID

object MapAlgebraProtocolSpray extends DefaultJsonProtocol {
  import MapAlgebra._

  implicit object ClassBoundaryTypeJsonFormat extends JsonFormat[ClassBoundaryType] {
    def write(cbt: ClassBoundaryType): JsValue = cbt match {
      case GreaterThan => JsString("greaterThan")
      case GreaterThanOrEqualTo => JsString("greaterThanOrEqualTo")
      case LessThan => JsString("lessThan")
      case LessThanOrEqualTo => JsString("lessThanOrEqualTo")
      case Exact => JsString("exact")
    }
    def read(js: JsValue): ClassBoundaryType = js match {
      case JsString("greaterThan") => GreaterThan
      case JsString("greaterThanOrEqualTo") => GreaterThanOrEqualTo
      case JsString("lessThan") => LessThan
      case JsString("lessThanOrEqualTo") => LessThanOrEqualTo
      case JsString("exact") => Exact
      case _ => deserializationError("Unable to parse ClassBoundaryType")
    }
  }

  implicit object ClassBreaksJsonFormat extends JsonFormat[ClassBreaks] {
    def write(cbreaks: ClassBreaks): JsValue = JsObject(
      "boundaryType" -> cbreaks.boundaryType.toJson,
      "classMap" -> cbreaks.classMap.map { case (d1, d2) => d1.toString -> d2 }.toMap.toJson
    )
    def read(js: JsValue): ClassBreaks = js.asJsObject.getFields("boundaryType", "classMap") match {
      case Seq(boundaryType, classMap) =>
        val cMap = classMap.asJsObject.fields.map { case (str, dbl) => str.toDouble -> dbl.convertTo[Double] }.toMap
        ClassBreaks(boundaryType.convertTo[ClassBoundaryType], cMap)
      case _ => deserializationError(s"Unable to deserialize ClassBreaks $js")
    }
  }
  //implicit val classBreaksJsonFormat = jsonFormat2(ClassBreaks.apply _)

  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsValue = JsString(uuid.toString)
    def read(js: JsValue): UUID = js match {
      case JsString(uuid) => UUID.fromString(uuid)
      case _ => deserializationError(s"Unable to parse UUID: ${js}")
    }
  }


  implicit class JsExtractors(val js: JsObject) {
    def getRasterAndBand(raster: String, band: String): (Option[UUID], Option[Int]) =
      js.getFields(raster, band) match {
        case Seq(r, b) => (Some(r.convertTo[UUID]), Some(b.convertTo[Int]))
        case Seq(r) => (Some(r.convertTo[UUID]), None)
        case _ =>
          deserializationError("Unable to deserialize raster (key: ${raster}) and band (key: ${band}) values")
      }

    def getRaster(raster: String): Option[UUID] =
      js.getFields(raster) match {
        case Seq(r) => Some(r.convertTo[UUID])
        case _ =>
          deserializationError(s"Unable to deserialize raster at key $raster in $js)")
      }

    def getValueAs[A: JsonFormat](key: String): Option[A] =
      js.getFields(key) match {
        case Seq(r) => scala.util.Try(r.convertTo[A]).toOption
        case _ => None
      }

    def getOrSetId: UUID =
      js.getFields("id") match {
        case Seq(null) => UUID.randomUUID()
        case Seq(id) => id.convertTo[UUID]
        case Seq() => UUID.randomUUID()
        case _ => deserializationError(s"Unable to deserialize node ID in $js")
      }
  }



  implicit val lazyMapAlgebraJsonFormat = lazyFormat[MapAlgebra](MapAlgebraJsonFormat)

  implicit object OperationJsonFormat extends JsonFormat[Operation] {
    def write(op: Operation): JsValue = op match {
      case add@Addition(args, id, label) => JsObject(
        "apply" -> JsString(add.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson
      )
      case subtract@Subtraction(args, id, label) => JsObject(
        "apply" -> JsString(subtract.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson
      )
      case multiply@Multiplication(args, id, label) => JsObject(
        "apply" -> JsString(multiply.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson
      )
      case divide@Division(args, id, label) => JsObject(
        "apply" -> JsString(divide.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson
      )
      case mask@Masking(args, id, label) => JsObject(
        "apply" -> JsString(mask.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson
      )
      case reclassify@Reclassification(args, id, label, classBreaks) => JsObject(
        "apply" -> JsString(reclassify.symbol),
        "id" -> id.toJson,
        "label" -> label.toJson,
        "args" -> args.toJson,
        "classBreaks" -> classBreaks.toJson
      )
    }

    def read(js: JsValue): Operation = {
      val jsObj = js.asJsObject
      val nodeLabel: Option[String] = jsObj.getValueAs[String]("label")
      val nodeId: UUID = jsObj.getOrSetId
      js.asJsObject.getFields("apply", "args") match {
        case Seq(JsString("+"), args) => Addition(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
        case Seq(JsString("-"), args) => Subtraction(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
        case Seq(JsString("*"), args) => Multiplication(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
        case Seq(JsString("/"), args) => Division(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
        case Seq(JsString("mask"), args) => Masking(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel)
        case Seq(JsString("reclassify"), args) =>
          val classBreaks: ClassBreaks = jsObj.getValueAs[ClassBreaks]("classBreaks").get
          Reclassification(args.convertTo[List[MapAlgebra]], nodeId, nodeLabel, classBreaks)
      }
    }
  }
  object MapAlgebraJsonFormat extends JsonFormat[MapAlgebra] {
    def write(root: MapAlgebra): JsValue =
      root match {
        case operation: Operation =>
          operation.toJson
        case source: Source => source match {
          case rasterSource: RasterSource => rasterSource match {
            case RFSceneSource(id, label, scene, band) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "scene" -> scene.toJson,
              "band" -> band.toJson,
              "type" -> JsString("raster")
            )
            case RFProjectSource(id, label, project, band) => JsObject(
              "id" -> id.toJson,
              "label" -> label.toJson,
              "project" -> project.toJson,
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
      val nodeLabel: Option[String] = jsObj.getValueAs[String]("label")
      val nodeId: UUID = jsObj.getOrSetId

      if (keys.contains("type")) { // This is a source
        jsObj.getFields("type") match {
          case Seq(JsString("raster")) =>
            if (keys.contains("project")) {
              val project = jsObj.getValueAs[UUID]("project")
              val band = jsObj.getValueAs[Int]("band")
              RFProjectSource(nodeId, nodeLabel, project, band)
            } else if (keys.contains("scene")) {
              val scene = jsObj.getValueAs[UUID]("scene")
              val band = jsObj.getValueAs[Int]("band")
              RFSceneSource(nodeId, nodeLabel, scene, band)
            } else if (keys.contains("toolRun")) {
              val toolRun = jsObj.getValueAs[UUID]("toolRun")
              MLToolSource(nodeId, nodeLabel, toolRun)
            } else if (keys.contains("ref")) {
              val ref = jsObj.getValueAs[UUID]("ref").get
              RefSource(nodeId, nodeLabel, ref)
            } else {
              deserializationError(s"Unable to deserialize raster source: ${jsvalue}")
            }
          case Seq(JsString("polygon")) =>
            val poly = jsObj.getValueAs[Polygon]("value")
            VectorSource(nodeId, nodeLabel, poly)
          case Seq(JsString("double")) =>
            val double = jsObj.getValueAs[Double]("value")
            DecimalSource(nodeId, nodeLabel, double)
          case Seq(JsString("int")) =>
            val int = jsObj.getValueAs[Int]("value")
            IntegralSource(nodeId, nodeLabel, int)
        }
      } else {                           // This is a function
        jsObj.convertTo[Operation]
      }
    }
  }
}

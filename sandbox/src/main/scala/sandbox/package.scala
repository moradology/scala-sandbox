package object sandbox {
  import spray.json._
  import DefaultJsonProtocol._

  import geotrellis.raster.render._

  import java.util.UUID

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
      case _ => deserializationError("Unable to parse UUID")
    }
  }

}

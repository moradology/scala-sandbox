package sandbox

import geotrellis.raster.Tile
import geotrellis.vector.Polygon
import geotrellis.vector.io._
import geotrellis.raster.render._

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.optics.JsonPath._
import io.circe.syntax._

import scala.util.Try
import java.util.UUID
import java.security.InvalidParameterException

object CirceHelpers {
  implicit class CirceMapAlgebraJsonMethods(val self: Json) {
    def _id: Option[UUID] = root.id.string.getOption(self).map(UUID.fromString(_))
    def _type: Option[String] = root.`type`.string.getOption(self)
    def _label: Option[String] = root.label.string.getOption(self)
    def _symbol: Option[String] = root.selectDynamic("apply").string.getOption(self)

    def _fields: Option[Seq[String]] = root.obj.getOption(self).map(_.fields)
  }

  implicit class CirceMapAlgebraHCursorMethods(val self: HCursor) {
    def _id: Option[UUID] = self.value._id
    def _type: Option[String] = self.value._type
    def _label: Option[String] = self.value._label
    def _symbol: Option[String] = self.value._symbol

    def _fields: Option[Seq[String]] = self.value._fields
  }

  implicit val decodeKeyDouble: KeyDecoder[Double] = new KeyDecoder[Double] {
    final def apply(key: String): Option[Double] = Try(key.toDouble).toOption
  }
  implicit val encodeKeyDouble: KeyEncoder[Double] = new KeyEncoder[Double] {
    final def apply(key: Double): String = key.toString
  }

  implicit lazy val classBreaksDecoder = Decoder.instance[ClassBreaks] { cbreaks =>
    val maybeBoundaryType =
      root.boundaryType.as[ClassBoundaryType].getOption(cbreaks.value)
    val maybeMap =
      root.classMap.as[Map[Double, Double]].getOption(cbreaks.value)

    ???
  }

  implicit lazy val classBoundaryDecoder: Decoder[ClassBoundaryType] =
    Decoder[String].map {
      case "greaterThan" => GreaterThan
      case "greaterThanOrEqualTo" => GreaterThanOrEqualTo
      case "lessThan" => LessThan
      case "lessThanOrEqualTo" => LessThanOrEqualTo
      case "exact" => Exact
      case unrecognized =>
        throw new InvalidParameterException(s"'$unrecognized' is not a recognized ClassBoundaryType")
    }
}


object MapAlgebraProtocolCirce {
  import CirceHelpers._

  implicit lazy val decodeAddition: Decoder[MapAlgebra.Addition] =
    Decoder.forProduct3("args", "id", "label")(MapAlgebra.Addition.apply)

  implicit lazy val decodeSubtraction: Decoder[MapAlgebra.Subtraction] =
    Decoder.forProduct3("args", "id", "label")(MapAlgebra.Subtraction.apply)

  implicit lazy val decodeDivision: Decoder[MapAlgebra.Division] =
    Decoder.forProduct3("args", "id", "label")(MapAlgebra.Division.apply)

  implicit lazy val decodeMultiplication: Decoder[MapAlgebra.Multiplication] =
    Decoder.forProduct3("args", "id", "label")(MapAlgebra.Multiplication.apply)

  implicit lazy val decodeMasking: Decoder[MapAlgebra.Masking] =
    Decoder.forProduct3("args", "id", "label")(MapAlgebra.Masking.apply)

  implicit lazy val decodeReclassification: Decoder[MapAlgebra.Reclassification] =
    Decoder.forProduct4("args", "id", "label", "classBreaks")(MapAlgebra.Reclassification.apply _)

  implicit lazy val decodeRasterSource = Decoder.instance[MapAlgebra.RasterSource] { ma =>
    ma._fields match {
      case src if src.contains("project") =>
        ma.as[MapAlgebra.RFProjectSource]
      case src if src.contains("scene") =>
        ma.as[MapAlgebra.RFSceneSource]
      case src if src.contains("toolRun") =>
        ma.as[MapAlgebra.MLToolSource]
      case src if src.contains("ref") =>
        ma.as[MapAlgebra.RefSource]
    }
  }

  implicit lazy val decodeVectorSource = Decoder.instance[MapAlgebra.VectorSource] { ??? }

  implicit lazy val decodeOperations = Decoder.instance[MapAlgebra.Operation] { ma =>
    ma._symbol match {
      case Some("+") => ma.as[MapAlgebra.Addition]
      case Some("-") => ma.as[MapAlgebra.Subtraction]
      case Some("/") => ma.as[MapAlgebra.Division]
      case Some("*") => ma.as[MapAlgebra.Multiplication]
      case Some("mask") => ma.as[MapAlgebra.Multiplication]
      case Some("reclassify") => ma.as[MapAlgebra.Multiplication]
      case Some(unrecognized) =>
        throw new InvalidParameterException(s"'$unrecognized' is not a recognized map algebra operation")
      case None =>
        throw new InvalidParameterException(s"Required 'apply' property not found on MapAlgebra operation")
    }
  }

  implicit lazy val decodeMapAlgebra = Decoder.instance[MapAlgebra] { ma =>
    ma._type match {
      case Some("raster") =>
        ma.as[MapAlgebra.RasterSource]
      case Some("vector") =>
        ma.as[MapAlgebra.VectorSource]
      case Some("double") =>
        ma.as[MapAlgebra.DecimalSource]
      case Some("int") =>
        ma.as[MapAlgebra.IntegralSource]
      case Some(unrecognized) =>
        throw new InvalidParameterException(s"'$unrecognized' is not a recognized map algebra data type")
      case None =>
        ma.as[MapAlgebra.Operation]
    }
  }
}

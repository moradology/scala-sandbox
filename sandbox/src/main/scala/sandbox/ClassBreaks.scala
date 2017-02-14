package sandbox

import spray.json._
import DefaultJsonProtocol._

import geotrellis.raster.render.ClassBoundaryType

case class ClassBreaks(boundaryType: ClassBoundaryType, classMap: Map[Double, Double])


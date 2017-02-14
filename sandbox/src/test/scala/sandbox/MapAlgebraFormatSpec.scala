package sandbox

import org.scalatest._
import spray.json._

import MapAlgebraProtocol._


class MapAlgebraFormatSpec extends FunSpec with Matchers {

  val ndviDiffRecipe =
    """
      |{
      |  "id": "3ba25089-cf30-42b9-a640-b66b2e77ae08",
      |  "apply": "-",
      |  "args": [
      |    {
      |      "id": "2ba25079-cf30-42b9-a640-b66b2e77ae07",
      |      "apply": "reclassify",
      |      "classBreaks": {
      |        "boundaryType": "lessThan",
      |        "classMap": { "3.3": 0, "5.0": 1, "6.1": 2 }
      |      },
      |      "args": [
      |        {
      |          "id": "2ba25079-cf30-42b9-a640-b66b2e77ae08",
      |          "display": "NDVI Time 0",
      |          "tag": "1",
      |          "apply": "/", "args": [
      |            {
      |              "id": "3ba25079-cf30-42b9-a640-b66b2e77ae08",
      |              "apply": "-",
      |              "args": [
      |                {
      |                  "id": "3ba35079-cf30-42b9-a640-b66b2e77be08",
      |                  "type": "raster",
      |                  "label": "red",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440000",
      |                  "band": 3
      |                },
      |                {
      |                  "id": "3ba35079-cf30-42b9-a640-b66b2e77ae08",
      |                  "type": "raster",
      |                  "label": "nir",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440000",
      |                  "band": 4
      |                }
      |              ]
      |            },
      |            {
      |              "id": "2ba25079-cf30-42b9-a640-b67b2e77ae08",
      |              "apply": "+",
      |              "args": [
      |                {
      |                  "id": "2ba35079-cf30-42b9-a640-b67b2e77ae08",
      |                  "type": "raster",
      |                  "label": "red",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440000",
      |                  "band": 3
      |                },
      |                {
      |                  "id": "2ba65079-cf30-42b9-a640-b67b2e77ae08",
      |                  "type": "raster",
      |                  "label": "nir",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440000",
      |                  "band": 4
      |                }
      |              ]
      |            }
      |          ]
      |        }
      |      ]
      |    },
      |    {
      |      "id": "2ba25079-cf30-42b9-a640-b66b2e77ae02",
      |      "apply": "reclassify",
      |      "classBreaks": {
      |        "boundaryType": "lessThan",
      |        "classMap": { "3.3": 0, "5.0": 1, "6.1": 2 }
      |      },
      |      "args": [
      |        {
      |          "id": "2ba25079-cf30-42b9-a640-b66b2e77ae09",
      |          "display": "NDVI Time 1",
      |          "tag": "2",
      |          "apply": "/",
      |          "args": [
      |            {
      |              "id": "2ba25079-cf30-42b9-a640-b66b1e77ae02",
      |              "apply": "-",
      |              "args": [
      |                {
      |                  "id": "2ba25079-cf30-42b9-a640-b66b2e77af05",
      |                  "type": "raster",
      |                  "label": "red",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440001",
      |                  "band": 3
      |                },
      |                {
      |                  "id": "2ba25079-cf30-42b9-a640-b66b2e77ae06",
      |                  "type": "raster",
      |                  "label": "nir",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440001",
      |                  "band": 4
      |                }
      |              ]
      |            },
      |            {
      |              "id": "2ba25079-cf30-42b9-a640-b66b2e77ae02",
      |              "apply": "+",
      |              "args": [
      |                {
      |                  "id": "2ba25079-cf30-42b9-a640-b66b2e77ae05",
      |                  "type": "raster",
      |                  "label": "red",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440001",
      |                  "band": 3
      |                },
      |                {
      |                  "id": "2ba25079-cf30-42b9-a640-b66b2e77ae04",
      |                  "type": "raster",
      |                  "label": "nir",
      |                  "mosaic": "123e4567-e89b-12d3-a456-426655440001",
      |                  "band": 4
      |                }
      |              ]
      |            }
      |          ]
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

  it("parses the ndvi difference recipe") {
    println(ndviDiffRecipe.parseJson.convertTo[MapAlgebra])
  }
}



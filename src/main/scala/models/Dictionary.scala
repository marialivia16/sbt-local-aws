package models

import cats.data.{NonEmptyList, Validated}
import cats.syntax.either._
import io.circe.{Error, Json, JsonObject}

final case class Dictionary(entries: Map[String, String])

object Dictionary {
  private val RegExPattern = "\\$\\{([a-zA-Z]+)}".r("name")

  def loadFromJson(json: Json): Dictionary = {
    val parameters: List[(String, Json)] =
      for {
        parametersJson <- json.hcursor.downField("Parameters").focus.toList
        parametersObject <- parametersJson.asObject.toList
        parameterField <- parametersObject.toList
      } yield parameterField

    Dictionary(parameters.flatMap {
      case (key, value) =>
        value.hcursor.downField("Default").as[String].toOption.map(key -> _) //TODO Maybe not string
    }.toMap
    )
  }

  def stringTransform(dictionary: Dictionary): String => String = (jsonStr: String) =>
    dictionary.entries.foldLeft(jsonStr) { case (acc, (key, value)) =>
      val toReplace = s"""$${$key}"""
      acc.replace(toReplace, value)
    }

//  private def extractFieldName(json: Json, dictionary: Dictionary): Validated[NonEmptyList[Error], String] = {
//    json.as[String].toValidatedNel.findValid {
//      json.hcursor.downField("Sub").as[String].map(
//        RegExPattern.replaceAllIn(_, m => dictionary.entries(m.group("name")))
//      ).toValidatedNel
//    }
//  }

  private def transform(js: Json, f: String => String): Json = js
    .mapString(f)
    .mapArray(_.map(transform(_, f)))
    .mapObject(obj => {
      val updatedObj = obj.toMap.map {
        case (k, v) => f(k) -> transform(v, f)
      }
      JsonObject.apply(updatedObj.toSeq: _*)
    })

  def applyDictionary(json: Json, dictionary: Dictionary): Json = transform(json, stringTransform(dictionary))

}

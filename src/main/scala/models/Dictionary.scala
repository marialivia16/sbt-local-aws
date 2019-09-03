package models

import io.circe.Json

final case class Dictionary(entries: Map[String, String])

object Dictionary {
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
}
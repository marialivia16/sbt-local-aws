package models

import io.circe.{Json, JsonObject}

final case class Dictionary(entries: Map[String, String])

object Dictionary {

  def apply(json: Json): Dictionary = {
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

  def replace(json: Json, dictionary: Dictionary): Json = replace(json, stringReplace(dictionary))

  def stringReplace(dictionary: Dictionary): String => String = (jsonStr: String) =>
    dictionary.entries.foldLeft(jsonStr) { case (acc, (key, value)) =>
      val toReplace = s"""$${$key}"""
      acc.replace(toReplace, value)
    }

  private def replace(js: Json, f: String => String): Json = js
    .mapString(f)
    .mapArray(_.map(replace(_, f)))
    .mapObject(obj => {
      val updatedObj = obj.toMap.map {
        case (k, v) => f(k) -> replace(v, f)
      }
      JsonObject.apply(updatedObj.toSeq: _*)
    })
    .withObject { o =>
      o.toList match {
        case List(("Sub", str)) => str
        case fields             => Json.obj(fields :_*)
      }
    }
}
